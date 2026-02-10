const KEY_BASE_URL = "ks.baseUrl";
const KEY_TENANT_ID = "ks.tenantId";

const $baseUrl = document.getElementById("baseUrl");
const $saveBaseUrl = document.getElementById("saveBaseUrl");
const $issueTenant = document.getElementById("issueTenant");
const $tenantId = document.getElementById("tenantId");
const $msg = document.getElementById("msg");

function show(msg) {
    $msg.textContent = msg;
}

async function load() {
    const data = await chrome.storage.sync.get([KEY_BASE_URL, KEY_TENANT_ID]);
    $baseUrl.value = data[KEY_BASE_URL] || "http://localhost:8089";
    $tenantId.textContent = data[KEY_TENANT_ID] || "-";
}

$saveBaseUrl.addEventListener("click", async () => {
    const v = ($baseUrl.value || "").trim().replace(/\/$/, "");
    if (!v) return show("Base URL을 입력하세요.");
    await chrome.storage.sync.set({ [KEY_BASE_URL]: v });
    show("Base URL 저장 완료");
});

$issueTenant.addEventListener("click", async () => {
    const data = await chrome.storage.sync.get([KEY_BASE_URL]);
    const baseUrl = (data[KEY_BASE_URL] || "http://localhost:8080").replace(/\/$/, "");

    show("발급 요청 중...");

    chrome.runtime.sendMessage(
        { type: "ISSUE_TENANT", baseUrl },
        async (resp) => {
            const err = chrome.runtime.lastError;
            if (err) return show("메시지 전송 실패: " + err.message);

            if (!resp?.ok) return show(resp?.error || "발급 실패");

            const tenantId = resp.tenantId;
            await chrome.storage.sync.set({ [KEY_TENANT_ID]: tenantId });
            $tenantId.textContent = tenantId;
            show("tenantId 발급 완료");
        }
    );
});

load();
console.log("options loaded");
