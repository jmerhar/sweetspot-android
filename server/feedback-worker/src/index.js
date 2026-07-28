/**
 * SweetSpot feedback Worker.
 *
 * Turns in-app "Report a problem" / "Send feedback" submissions into GitHub issues, and (Phase 2)
 * emails the reporter — if they opted in — when their issue gets a comment or is closed.
 *
 * Routes:
 *   POST /report   — app submits a report/feedback; creates a labelled GitHub issue.
 *   POST /webhook  — GitHub webhook (issues, issue_comment); emails the opted-in reporter via Brevo.
 *   GET  /         — health check.
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
  if (email && email.length > 0) {
    // 400 days; the reporter can ask to be removed (Phase 2 unsubscribe).
    await env.FEEDBACK_KV.put(`issue:${issue.number}`, email, { expirationTtl: 34560000 });
  }

  return json({ number: issue.number, url: issue.html_url }, 201);
}

/** Assembles the issue body, appending diagnostics (bug reports) in a collapsible block. */
function buildIssueBody(body, diagnostics) {
  let out = `${body}\n\n<sub>Submitted from the SweetSpot app.</sub>`;
  if (diagnostics && diagnostics.trim().length > 0) {
    out += `\n\n<details><summary>Diagnostics</summary>\n\n\`\`\`\n${diagnostics.trim()}\n\`\`\`\n</details>`;
  }
  return out;
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

  const email = await env.FEEDBACK_KV.get(`issue:${notice.number}`);
  if (!email) return json({ ok: true, no_subscriber: true }, 200);

  // Respond to GitHub immediately; deliver the email in the background.
  ctx.waitUntil(sendEmail(env, email, notice));
  return json({ ok: true }, 200);
}

/**
 * Decides whether a webhook event warrants notifying the reporter, and what to say.
 * Returns `{ number, title, url, kind, text }` or null when nothing should be sent.
 */
function notificationFor(event, data, botLogin) {
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
async function sendEmail(env, to, notice) {
  const heading = notice.kind === "closed"
    ? `Your SweetSpot report was closed`
    : `New reply to your SweetSpot report`;
  const excerpt = notice.text
    ? `<blockquote>${escapeHtml(truncate(notice.text, 500))}</blockquote>`
    : "";
  const html =
    `<p>${heading} — <strong>${escapeHtml(notice.title)}</strong> (#${notice.number}).</p>` +
    excerpt +
    `<p><a href="${notice.url}">View your report on GitHub</a></p>` +
    `<hr><p style="color:#888;font-size:12px">You're receiving this because you asked to be ` +
    `notified about this report in the SweetSpot app. Reply to this email to stop.</p>`;
  const text =
    `${heading} — ${notice.title} (#${notice.number}).\n\n` +
    (notice.text ? `${truncate(notice.text, 500)}\n\n` : "") +
    `View your report: ${notice.url}\n\n` +
    `You're receiving this because you asked to be notified in the SweetSpot app. Reply to stop.`;

  const res = await fetch(BREVO_API, {
    method: "POST",
    headers: {
      "api-key": env.BREVO_API_KEY,
      "Content-Type": "application/json",
      "Accept": "application/json",
    },
    body: JSON.stringify({
      sender: { name: env.BREVO_SENDER_NAME, email: env.BREVO_SENDER_EMAIL },
      to: [{ email: to }],
      replyTo: { email: env.REPLY_TO_EMAIL, name: env.BREVO_SENDER_NAME },
      subject: `${heading} (#${notice.number})`,
      htmlContent: html,
      textContent: text,
    }),
  });
  if (!res.ok) {
    console.error(JSON.stringify({ at: "brevo_send", status: res.status }));
  }
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

function intVar(v, fallback) {
  const n = parseInt(v ?? "", 10);
  return Number.isFinite(n) && n > 0 ? n : fallback;
}

function truncate(s, n) {
  return s.length > n ? `${s.slice(0, n)}…` : s;
}

function escapeHtml(s) {
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
