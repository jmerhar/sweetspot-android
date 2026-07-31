import { describe, it, expect } from "vitest";
import {
  readSubscription,
  notificationFor,
  buildIssueBody,
  intVar,
  truncate,
  escapeHtml,
  handleUnsubscribeGet,
} from "../src/index.js";

/** A minimal Workers-KV stub: `get(key)` resolves the pre-seeded string (or null). */
function kv(entries = {}) {
  return { FEEDBACK_KV: { get: async (key) => (key in entries ? entries[key] : null) } };
}

describe("readSubscription", () => {
  // Regression guard: a no-email report stores `{email:null, token}`. The token — not the email — is
  // the capability that authorises in-app replies and the unsubscribe link, so it must be returned even
  // when the email is null. A guard that only accepted a string email once dropped it and 403'd every
  // reply for the common (no-email) report.
  it("returns the token when the email is null (no-email report)", async () => {
    const env = kv({ "issue:8": JSON.stringify({ email: null, token: "T8" }) });
    expect(await readSubscription(env, 8)).toEqual({ email: null, token: "T8" });
  });

  it("returns both email and token when the reporter opted in", async () => {
    const env = kv({ "issue:9": JSON.stringify({ email: "a@b.com", token: "T9" }) });
    expect(await readSubscription(env, 9)).toEqual({ email: "a@b.com", token: "T9" });
  });

  it("returns a null token when the stored object has no token", async () => {
    const env = kv({ "issue:10": JSON.stringify({ email: "a@b.com" }) });
    expect(await readSubscription(env, 10)).toEqual({ email: "a@b.com", token: null });
  });

  it("treats a non-JSON value as a legacy plain-email entry (no token)", async () => {
    const env = kv({ "issue:11": "legacy@b.com" });
    expect(await readSubscription(env, 11)).toEqual({ email: "legacy@b.com", token: null });
  });

  it("returns null when the key is absent", async () => {
    expect(await readSubscription(kv(), 12)).toBeNull();
  });
});

describe("notificationFor", () => {
  const issue = { number: 5, title: "Bug", html_url: "https://x/5" };

  it("skips the bot's own comments (no notification loop)", () => {
    const data = { action: "created", issue, comment: { user: { login: "sweetspot-support" }, body: "hi" } };
    expect(notificationFor("issue_comment", data, "sweetspot-support")).toBeNull();
  });

  it("notifies on a non-bot comment", () => {
    const data = { action: "created", issue, comment: { user: { login: "jmerhar" }, body: "on it" } };
    const n = notificationFor("issue_comment", data, "sweetspot-support");
    expect(n).toMatchObject({ number: 5, kind: "comment", text: "on it" });
  });

  it("notifies when an issue is closed", () => {
    const n = notificationFor("issues", { action: "closed", issue }, "sweetspot-support");
    expect(n).toMatchObject({ number: 5, kind: "closed" });
  });

  it("ignores unrelated events", () => {
    expect(notificationFor("issues", { action: "labeled", issue }, "sweetspot-support")).toBeNull();
    expect(notificationFor("issue_comment", { action: "edited", issue }, "sweetspot-support")).toBeNull();
  });
});

describe("buildIssueBody", () => {
  it("appends the app footer and no diagnostics block when there are none", () => {
    const out = buildIssueBody("It crashed", null);
    expect(out).toContain("It crashed");
    expect(out).toContain("Submitted from the SweetSpot app.");
    expect(out).not.toContain("<details>");
  });

  it("adds a collapsible diagnostics block when diagnostics are present", () => {
    const out = buildIssueBody("It crashed", "App: 6.6\nAndroid: 16");
    expect(out).toContain("<details><summary>Diagnostics</summary>");
    expect(out).toContain("App: 6.6");
  });

  it("fences with extra backticks when the diagnostics contain a code fence", () => {
    const out = buildIssueBody("It crashed", "```\nleak\n```");
    // A 4-backtick fence wraps content that itself contains ```, so the
    // block can't be terminated early.
    expect(out).toContain("````\n```\nleak\n```\n````");
  });
});

describe("intVar", () => {
  it("parses a positive integer", () => expect(intVar("5", 9)).toBe(5));
  it("falls back on empty / non-numeric / non-positive input", () => {
    expect(intVar("", 9)).toBe(9);
    expect(intVar("abc", 9)).toBe(9);
    expect(intVar("0", 9)).toBe(9);
    expect(intVar(undefined, 9)).toBe(9);
  });
});

describe("truncate", () => {
  it("leaves a short string unchanged", () => expect(truncate("hi", 5)).toBe("hi"));
  it("truncates a long string with an ellipsis", () => expect(truncate("hello world", 5)).toBe("hello…"));
});

describe("escapeHtml", () => {
  it("escapes HTML-significant characters", () => {
    expect(escapeHtml(`<a href="x">&'`)).toBe("&lt;a href=&quot;x&quot;&gt;&amp;&#39;");
  });
});

describe("handleUnsubscribeGet", () => {
  it("rejects a malformed link", async () => {
    const res = handleUnsubscribeGet(new URL("https://feedback.sweetspot.today/unsubscribe?issue=abc&token="));
    expect(res.status).toBe(200);
    expect(await res.text()).toContain("invalid");
  });

  it("renders a confirmation form for a well-formed link", async () => {
    const res = handleUnsubscribeGet(new URL("https://feedback.sweetspot.today/unsubscribe?issue=5&token=T5"));
    const html = await res.text();
    expect(html).toContain("Unsubscribe");
    expect(html).toContain("issue=5");
    expect(html).toContain("token=T5");
  });
});
