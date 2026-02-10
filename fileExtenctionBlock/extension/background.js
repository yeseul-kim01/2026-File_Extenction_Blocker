chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => {
    if (msg?.type !== "ISSUE_TENANT") return;

    (async () => {
        try {
            const baseUrl = String(msg.baseUrl || "").replace(/\/$/, "");
            const url = `${baseUrl}/api/tenants/issue`;

            const res = await fetch(url, { method: "POST" });
            const body = await res.json().catch(() => ({}));

            if (!res.ok) {
                return sendResponse({ ok: false, error: body?.message || `HTTP ${res.status}` });
            }

            // 응답 필드명 맞추기: tenantId 또는 id 등
            const tenantId = body.tenantId || body.id || body.tenant || "";
            if (!tenantId) return sendResponse({ ok: false, error: "응답에 tenantId가 없습니다." });

            return sendResponse({ ok: true, tenantId });
        } catch (e) {
            return sendResponse({ ok: false, error: e?.message || "unknown error" });
        }
    })();

    return true; // async 응답
});
