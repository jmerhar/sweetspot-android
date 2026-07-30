/**
 * SweetSpot feedback Worker.
 *
 * Turns in-app "Report a problem" / "Send feedback" submissions into GitHub issues, and (Phase 2)
 * emails the reporter — if they opted in — when their issue gets a comment or is closed.
 *
 * Routes:
 *   POST /report        — app submits a report/feedback; creates a labelled GitHub issue.
 *   POST /webhook       — GitHub webhook (issues, issue_comment); emails the opted-in reporter via Brevo.
 *   POST /reply         — app posts a comment on its own report (bot-authored), authorised by the token.
 *   GET  /unsubscribe   — confirmation page for the tokenized unsubscribe link in each notification.
 *   POST /unsubscribe   — clears the reporter's stored email (form submit, or RFC 8058 one-click).
 *   GET  /              — health check.
 *
 * The app never holds a GitHub token — it only POSTs here (submit) and reads *public* issue state
 * directly from GitHub (tracking). Secrets live only in this Worker: GITHUB_TOKEN, WEBHOOK_SECRET,
 * BREVO_API_KEY (set via `wrangler secret put`). See README.md.
 */

const GITHUB_API = "https://api.github.com";
const BREVO_API = "https://api.brevo.com/v3/smtp/email";
const USER_AGENT = "sweetspot-feedback-worker";
const CATEGORY_LABEL = { bug: "bug", feedback: "enhancement" };
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    try {
      if (request.method === "GET" && url.pathname === "/") {
        return new Response("ok", { status: 200 });
      }
      if (request.method === "POST" && url.pathname === "/report") {
        return await handleReport(request, env);
      }
      if (request.method === "POST" && url.pathname === "/webhook") {
        return await handleWebhook(request, env, ctx);
      }
      if (request.method === "POST" && url.pathname === "/reply") {
        return await handleReply(request, env);
      }
      if (request.method === "GET" && url.pathname === "/unsubscribe") {
        return handleUnsubscribeGet(url);
      }
      if (request.method === "POST" && url.pathname === "/unsubscribe") {
        return await handleUnsubscribePost(request, url, env);
      }
      return json({ error: "not_found" }, 404);
    } catch (err) {
      console.error(JSON.stringify({ at: url.pathname, error: String(err?.message ?? err) }));
      return json({ error: "server_error" }, 500);
    }
  },
};

/** Creates a GitHub issue from an app submission. */
async function handleReport(request, env) {
  if (!request.headers.get("content-type")?.includes("application/json")) {
    return json({ error: "expected_json" }, 415);
  }

  let payload;
  try {
    payload = await request.json();
  } catch {
    return json({ error: "invalid_json" }, 400);
  }

  const maxSubject = intVar(env.MAX_SUBJECT, 120);
  const maxBody = intVar(env.MAX_BODY, 4000);

  const category = String(payload.category ?? "").toLowerCase();
  const subject = String(payload.subject ?? "").trim().replace(/[\r\n]+/g, " ");
  const body = String(payload.body ?? "").trim();
  const diagnostics = payload.diagnostics != null ? String(payload.diagnostics) : null;
  const email = payload.email != null ? String(payload.email).trim() : null;

  if (!(category in CATEGORY_LABEL)) return json({ error: "invalid_category" }, 400);
  if (subject.length === 0 || subject.length > maxSubject) return json({ error: "invalid_subject" }, 400);
  if (body.length === 0 || body.length > maxBody) return json({ error: "invalid_body" }, 400);
  if (diagnostics != null && diagnostics.length > maxBody) return json({ error: "invalid_diagnostics" }, 400);
  if (email != null && email.length > 0 && (email.length > 254 || !EMAIL_RE.test(email))) {
    return json({ error: "invalid_email" }, 400);
  }

  // Coarse per-IP daily rate limit (KV, eventually consistent — fine for abuse throttling).
  const ip = request.headers.get("CF-Connecting-IP") ?? "unknown";
  const limit = intVar(env.RATE_LIMIT_PER_DAY, 5);
  const rlKey = `rl:${ip}:${new Date().toISOString().slice(0, 10)}`;
  const used = parseInt((await env.FEEDBACK_KV.get(rlKey)) ?? "0", 10) || 0;
  if (used >= limit) return json({ error: "rate_limited" }, 429);

  // Compose the issue. The reporter's email is NEVER written into the (public) issue.
  const labels = ["from-app", CATEGORY_LABEL[category]];
  const issueBody = buildIssueBody(body, diagnostics);

  const ghRes = await fetch(`${GITHUB_API}/repos/${env.GITHUB_OWNER}/${env.GITHUB_REPO}/issues`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${env.GITHUB_TOKEN}`,
      "Accept": "application/vnd.github+json",
      "X-GitHub-Api-Version": "2022-11-28",
      "User-Agent": USER_AGENT,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ title: subject, body: issueBody, labels }),
  });

  if (ghRes.status !== 201) {
    console.error(JSON.stringify({ at: "create_issue", status: ghRes.status }));
    return json({ error: "upstream_error" }, 502);
  }

  const issue = await ghRes.json();

  // Only now (success) spend the rate-limit slot, and store the opt-in email for notifications.
  await env.FEEDBACK_KV.put(rlKey, String(used + 1), { expirationTtl: 172800 });

  // Always store a per-report token (400-day TTL). It's the single capability for this report: the app
  // holds it (returned below) to post in-app replies via /reply, and it also backs the emailed
  // unsubscribe link. The email is kept only if the reporter opted into notifications.
  const token = crypto.randomUUID();
  const stored = JSON.stringify({ email: email && email.length > 0 ? email : null, token });
  await env.FEEDBACK_KV.put(`issue:${issue.number}`, stored, { expirationTtl: 34560000 });

  return json({ number: issue.number, url: issue.html_url, replyToken: token }, 201);
}

/** Assembles the issue body, appending diagnostics (bug reports) in a collapsible block. */
export function buildIssueBody(body, diagnostics) {
  let out = `${body}\n\n<sub>Submitted from the SweetSpot app.</sub>`;
  if (diagnostics && diagnostics.trim().length > 0) {
    out += `\n\n<details><summary>Diagnostics</summary>\n\n\`\`\`\n${diagnostics.trim()}\n\`\`\`\n</details>`;
  }
  return out;
}

/**
 * Posts a comment on an issue as the bot, on behalf of the reporter. Authorised by the report's token
 * (the same one returned to the app at /report time), so only a device that submitted the report can
 * reply. The comment is prefixed to mark it as coming from the reporter via the app.
 */
async function handleReply(request, env) {
  if (!request.headers.get("content-type")?.includes("application/json")) {
    return json({ error: "expected_json" }, 415);
  }
  let payload;
  try {
    payload = await request.json();
  } catch {
    return json({ error: "invalid_json" }, 400);
  }

  const number = Number.parseInt(String(payload.issue ?? ""), 10);
  const token = String(payload.token ?? "");
  const body = String(payload.body ?? "").trim();
  const maxBody = intVar(env.MAX_BODY, 4000);
  if (!Number.isInteger(number) || number <= 0) return json({ error: "invalid_issue" }, 400);
  if (token.length === 0) return json({ error: "invalid_token" }, 400);
  if (body.length === 0 || body.length > maxBody) return json({ error: "invalid_body" }, 400);

  // Per-IP daily rate limit, separate from /report (replies are token-gated, so a looser cap is fine).
  const ip = request.headers.get("CF-Connecting-IP") ?? "unknown";
  const limit = intVar(env.REPLY_RATE_LIMIT_PER_DAY, 20);
  const rlKey = `rlr:${ip}:${new Date().toISOString().slice(0, 10)}`;
  const used = parseInt((await env.FEEDBACK_KV.get(rlKey)) ?? "0", 10) || 0;
  if (used >= limit) return json({ error: "rate_limited" }, 429);

  // Authorise: the presented token must match the one stored for this issue (constant-time).
  const sub = await readSubscription(env, number);
  if (!sub?.token || !timingSafeEqualStr(token, sub.token)) return json({ error: "forbidden" }, 403);

  const commentBody = `💬 **Reporter (via app):**\n\n${body}`;
  const ghRes = await fetch(
    `${GITHUB_API}/repos/${env.GITHUB_OWNER}/${env.GITHUB_REPO}/issues/${number}/comments`,
    {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${env.GITHUB_TOKEN}`,
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
        "User-Agent": USER_AGENT,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ body: commentBody }),
    },
  );
  if (ghRes.status !== 201) {
    console.error(JSON.stringify({ at: "create_comment", status: ghRes.status }));
    return json({ error: "upstream_error" }, 502);
  }

  await env.FEEDBACK_KV.put(rlKey, String(used + 1), { expirationTtl: 172800 });
  const comment = await ghRes.json();
  return json({ ok: true, url: comment.html_url }, 201);
}

/** GitHub webhook: notify the opted-in reporter on new comments / issue close. */
async function handleWebhook(request, env, ctx) {
  const raw = await request.text();
  const signature = request.headers.get("X-Hub-Signature-256");
  if (!(await verifySignature(env.WEBHOOK_SECRET, raw, signature))) {
    return json({ error: "bad_signature" }, 401);
  }

  const event = request.headers.get("X-GitHub-Event");
  let data;
  try {
    data = JSON.parse(raw);
  } catch {
    return json({ error: "invalid_json" }, 400);
  }

  const notice = notificationFor(event, data, env.BOT_LOGIN);
  if (!notice) return json({ ok: true, skipped: true }, 200);

  const sub = await readSubscription(env, notice.number);
  if (!sub || !sub.email) return json({ ok: true, no_subscriber: true }, 200);

  // A tokenized, per-report unsubscribe link (only for entries that carry a token — legacy
  // plain-email entries fall back to "reply to stop"). The token is the capability, so the link
  // is safe to email and can't be used to unsubscribe anyone else.
  const unsubscribeUrl = sub.token
    ? `${new URL(request.url).origin}/unsubscribe?issue=${notice.number}&token=${encodeURIComponent(sub.token)}`
    : null;

  // Respond to GitHub immediately; deliver the email in the background.
  ctx.waitUntil(sendEmail(env, sub.email, notice, unsubscribeUrl));
  return json({ ok: true }, 200);
}

/**
 * Decides whether a webhook event warrants notifying the reporter, and what to say.
 * Returns `{ number, title, url, kind, text }` or null when nothing should be sent.
 */
export function notificationFor(event, data, botLogin) {
  if (event === "issue_comment" && data.action === "created") {
    // Skip the bot's own automated comments (avoids notification loops).
    if (data.comment?.user?.login === botLogin) return null;
    return {
      number: data.issue.number,
      title: data.issue.title,
      url: data.issue.html_url,
      kind: "comment",
      text: data.comment?.body ?? "",
    };
  }
  if (event === "issues" && data.action === "closed") {
    return {
      number: data.issue.number,
      title: data.issue.title,
      url: data.issue.html_url,
      kind: "closed",
      text: "",
    };
  }
  return null;
}

/** Sends the notification via Brevo's transactional email API. */
async function sendEmail(env, to, notice, unsubscribeUrl) {
  const heading = notice.kind === "closed"
    ? `Your SweetSpot report was closed`
    : `New reply to your SweetSpot report`;
  const excerpt = notice.text
    ? `<blockquote>${escapeHtml(truncate(notice.text, 500))}</blockquote>`
    : "";
  const htmlFooter = unsubscribeUrl
    ? `You're receiving this because you asked to be notified about this report in the SweetSpot app. ` +
      `<a href="${escapeHtml(unsubscribeUrl)}">Unsubscribe from notifications about this report</a>.`
    : `You're receiving this because you asked to be notified about this report in the SweetSpot app. ` +
      `Reply to this email to stop.`;
  const textFooter = unsubscribeUrl
    ? `You're receiving this because you asked to be notified in the SweetSpot app.\n` +
      `Unsubscribe from notifications about this report: ${unsubscribeUrl}`
    : `You're receiving this because you asked to be notified in the SweetSpot app. Reply to stop.`;
  const html =
    `<p>${heading} — <strong>${escapeHtml(notice.title)}</strong> (#${notice.number}).</p>` +
    excerpt +
    `<p><a href="${notice.url}">View your report on GitHub</a></p>` +
    `<hr><p style="color:#888;font-size:12px">${htmlFooter}</p>`;
  const text =
    `${heading} — ${notice.title} (#${notice.number}).\n\n` +
    (notice.text ? `${truncate(notice.text, 500)}\n\n` : "") +
    `View your report: ${notice.url}\n\n` +
    textFooter;

  const payload = {
    sender: { name: env.BREVO_SENDER_NAME, email: env.BREVO_SENDER_EMAIL },
    to: [{ email: to }],
    replyTo: { email: env.REPLY_TO_EMAIL, name: env.BREVO_SENDER_NAME },
    subject: `${heading} (#${notice.number})`,
    htmlContent: html,
    textContent: text,
  };
  // RFC 8058 one-click unsubscribe: mail clients surface a native "Unsubscribe" button that POSTs
  // to this URL (body `List-Unsubscribe=One-Click`), which our POST /unsubscribe handles.
  if (unsubscribeUrl) {
    payload.headers = {
      "List-Unsubscribe": `<${unsubscribeUrl}>`,
      "List-Unsubscribe-Post": "List-Unsubscribe=One-Click",
    };
  }

  const res = await fetch(BREVO_API, {
    method: "POST",
    headers: {
      "api-key": env.BREVO_API_KEY,
      "Content-Type": "application/json",
      "Accept": "application/json",
    },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    console.error(JSON.stringify({ at: "brevo_send", status: res.status }));
  }
}

/**
 * Reads a stored subscription for an issue. New entries are `{email, token}` JSON; older entries
 * may be a bare email string (no token) — those get a null token and fall back to "reply to stop".
 */
export async function readSubscription(env, number) {
  const raw = await env.FEEDBACK_KV.get(`issue:${number}`);
  if (!raw) return null;
  try {
    const obj = JSON.parse(raw);
    if (obj && typeof obj === "object") {
      // Structured entry `{email, token}`. The email is null when the reporter didn't opt into
      // notifications, but the token is still present — it authorises in-app replies and backs the
      // unsubscribe link, so it must be returned regardless of the email being absent.
      return {
        email: typeof obj.email === "string" ? obj.email : null,
        token: typeof obj.token === "string" ? obj.token : null,
      };
    }
  } catch {
    // Not JSON — treat the whole value as a legacy plain-email entry.
  }
  return { email: raw, token: null };
}

/** Unsubscribe confirmation page (GET is non-mutating, so link prefetchers can't unsubscribe). */
export function handleUnsubscribeGet(url) {
  const issue = url.searchParams.get("issue") ?? "";
  const token = url.searchParams.get("token") ?? "";
  if (!/^\d+$/.test(issue) || token.length === 0) {
    return htmlResponse(`<h1>SweetSpot</h1><p>This unsubscribe link is invalid.</p>`);
  }
  const action = `/unsubscribe?issue=${encodeURIComponent(issue)}&token=${encodeURIComponent(token)}`;
  return htmlResponse(
    `<h1>Unsubscribe</h1>` +
    `<p>Stop receiving email notifications about SweetSpot report #${escapeHtml(issue)}?</p>` +
    `<form method="POST" action="${escapeHtml(action)}">` +
    `<button type="submit" style="padding:.6rem 1.2rem;font-size:1rem;cursor:pointer">Unsubscribe</button>` +
    `</form>`,
  );
}

/**
 * Clears the stored email for an issue when the token matches — from the confirmation form or an
 * RFC 8058 one-click POST. Responds identically whether or not an entry was removed, so the endpoint
 * reveals nothing about which issues have subscribers.
 */
async function handleUnsubscribePost(request, url, env) {
  const issue = url.searchParams.get("issue") ?? "";
  const token = url.searchParams.get("token") ?? "";
  const oneClick = await isOneClickUnsubscribe(request);

  if (/^\d+$/.test(issue) && token.length > 0) {
    const sub = await readSubscription(env, issue);
    if (sub?.token && timingSafeEqualStr(token, sub.token)) {
      // Clear the email but keep the token, so notifications stop while in-app replies still work.
      const value = JSON.stringify({ email: null, token: sub.token });
      await env.FEEDBACK_KV.put(`issue:${issue}`, value, { expirationTtl: 34560000 });
    }
  }

  if (oneClick) return new Response("Unsubscribed", { status: 200 });
  return htmlResponse(
    `<h1>Unsubscribed</h1>` +
    `<p>You'll no longer receive email notifications about this SweetSpot report. ` +
    `You can still view it any time on GitHub.</p>`,
  );
}

/** True when the POST is an RFC 8058 one-click unsubscribe (native mail-client button). */
async function isOneClickUnsubscribe(request) {
  try {
    return (await request.text()).includes("List-Unsubscribe=One-Click");
  } catch {
    return false;
  }
}

/** Constant-time string comparison (avoids leaking the token via response timing). */
function timingSafeEqualStr(a, b) {
  const ea = new TextEncoder().encode(a);
  const eb = new TextEncoder().encode(b);
  if (ea.length !== eb.length) return false;
  return crypto.subtle.timingSafeEqual(ea, eb);
}

/** Minimal styled HTML page for the browser-facing unsubscribe flow. */
function htmlResponse(bodyHtml, status = 200) {
  const doc =
    `<!doctype html><html lang="en"><head><meta charset="utf-8">` +
    `<meta name="viewport" content="width=device-width,initial-scale=1"><title>SweetSpot</title></head>` +
    `<body style="font-family:system-ui,-apple-system,sans-serif;max-width:32rem;margin:3rem auto;` +
    `padding:0 1rem;line-height:1.6;color:#191C20">${bodyHtml}</body></html>`;
  return new Response(doc, { status, headers: { "Content-Type": "text/html; charset=utf-8" } });
}

/** Constant-time HMAC-SHA256 verification of GitHub's `X-Hub-Signature-256` header. */
async function verifySignature(secret, payload, header) {
  if (!secret || !header || !header.startsWith("sha256=")) return false;
  const sigBytes = hexToBytes(header.slice("sha256=".length));
  if (!sigBytes) return false;
  const key = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["verify"],
  );
  return crypto.subtle.verify("HMAC", key, sigBytes, new TextEncoder().encode(payload));
}

function hexToBytes(hex) {
  if (hex.length === 0 || hex.length % 2 !== 0 || /[^0-9a-fA-F]/.test(hex)) return null;
  const out = new Uint8Array(hex.length / 2);
  for (let i = 0; i < out.length; i++) out[i] = parseInt(hex.substr(i * 2, 2), 16);
  return out;
}

export function intVar(v, fallback) {
  const n = parseInt(v ?? "", 10);
  return Number.isFinite(n) && n > 0 ? n : fallback;
}

export function truncate(s, n) {
  return s.length > n ? `${s.slice(0, n)}…` : s;
}

export function escapeHtml(s) {
  return s.replace(/[&<>"']/g, (c) => (
    { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]
  ));
}

function json(obj, status) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}
