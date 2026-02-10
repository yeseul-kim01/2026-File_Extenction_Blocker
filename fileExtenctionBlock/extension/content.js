
const LOG_PREFIX = "[FileExtensionBlocker]";
const TTL_MS = 60_000; // 60초 캐시

let cachedPolicy = null;
let cachedAt = 0;

const KEY_BASE_URL = "ks.baseUrl";
const KEY_TENANT_ID = "ks.tenantId";

const log = (...args) => console.log(LOG_PREFIX, ...args);
const warn = (...args) => console.warn(LOG_PREFIX, ...args);
const error = (...args) => console.error(LOG_PREFIX, ...args);

log("content script loaded:", location.href);

function normalizeExt(name) {
    const v = (name || "").trim();
    const idx = v.lastIndexOf(".");
    if (idx < 0) return "";
    return v.slice(idx + 1).toLowerCase();
}

function isBlocked(ext, policy) {
    if (!ext || !policy) return false;
    const fixed = policy.fixed || [];
    const custom = policy.custom || [];

    if (fixed.some((f) => (f.ext || "").toLowerCase() === ext && !!f.blocked)) return true;
    if (custom.some((c) => (typeof c === "string" ? c : c.ext).toLowerCase() === ext)) return true;

    return false;
}

async function getConfig() {
    const data = await chrome.storage.sync.get([KEY_TENANT_ID, KEY_BASE_URL]);
    const tenantId = data[KEY_TENANT_ID];
    const apiBaseUrl = data[KEY_BASE_URL];
    return { tenantId, apiBaseUrl };
}

function getCachedPolicyIfFresh() {
    const now = Date.now();
    if (!cachedPolicy) return null;
    if (now - cachedAt > TTL_MS) return null;
    return cachedPolicy;
}

/**
 * 정책 비동기 갱신 (이 함수는 이벤트 핸들러 안에서 await 하지 말 것)
 */
async function refreshPolicy() {
    const { tenantId, apiBaseUrl } = await getConfig();

    if (!tenantId || !apiBaseUrl) {
        warn("tenantId or baseUrl missing -> allow upload");
        cachedPolicy = null;
        cachedAt = 0;
        return;
    }

    const url = `${apiBaseUrl.replace(/\/$/, "")}/api/policy/extensions`;
    log("refresh policy:", url, { tenantId });

    try {
        const res = await fetch(url, {
            method: "GET",
            headers: { "X-Tenant-Id": tenantId },
        });

        if (!res.ok) {
            warn("policy fetch failed:", res.status);
            return;
        }

        cachedPolicy = await res.json();
        cachedAt = Date.now();
        log("policy refreshed:", cachedPolicy);
    } catch (e) {
        error("policy fetch error:", e);
    }
}

function hardResetFileInput(inputEl) {
    try { inputEl.value = ""; } catch {}
    try { if (inputEl.form) inputEl.form.reset(); } catch {}

    try {
        inputEl.disabled = true;
        requestAnimationFrame(() => {
            inputEl.disabled = false;
        });
    } catch {}
}


function showToast(message) {
    try {
        const id = "feb-toast";
        let el = document.getElementById(id);

        if (!el) {
            el = document.createElement("div");
            el.id = id;
            el.style.position = "fixed";
            el.style.top = "16px";
            el.style.right = "16px";
            el.style.zIndex = "2147483647";
            el.style.background = "rgba(20,20,20,0.92)";
            el.style.color = "#fff";
            el.style.padding = "12px 14px";
            el.style.borderRadius = "10px";
            el.style.fontSize = "14px";
            el.style.lineHeight = "1.4";
            el.style.boxShadow = "0 8px 20px rgba(0,0,0,0.25)";
            el.style.maxWidth = "360px";
            el.style.wordBreak = "break-word";
            document.documentElement.appendChild(el);
        }

        el.textContent = message;
        el.style.opacity = "1";

        clearTimeout(el.__timer);
        el.__timer = setTimeout(() => {
            el.style.opacity = "0";
        }, 2500);
    } catch {
        // 마지막 fallback
        alert(message);
    }
}

function blockEventHard(e) {
    // 이게 핵심: 사이트 핸들러(React 포함)가 파일을 state에 넣기 전에 끊어야 함
    try { e.preventDefault(); } catch {}
    try { e.stopPropagation(); } catch {}
    try { e.stopImmediatePropagation(); } catch {}
}

/**
 * 드래그앤드롭 업로드 차단 (동기 캐시 기반)
 */
document.addEventListener(
    "drop",
    (e) => {
        const dt = e.dataTransfer;
        if (!dt) return;

        const files = Array.from(dt.files || []);
        if (files.length === 0) return;

        // 캐시 없으면 fail-open (업무 영향 최소화) + 백그라운드로 갱신
        const policy = getCachedPolicyIfFresh();
        if (!policy) {
            refreshPolicy();
            return;
        }

        for (const f of files) {
            const ext = normalizeExt(f.name);
            if (!ext) continue;

            const blocked = isBlocked(ext, policy);
            if (blocked) {
                blockEventHard(e);
                warn("blocked upload (drop):", ext, f.name);
                showToast(`.${ext} 파일은 업로드할 수 없습니다. (정책 차단)`);
                return;
            }
        }
    },
    true
);

/**
 * dragover에서 기본 동작 막아야 drop이 정상 동작하는 사이트가 있음
 */
document.addEventListener(
    "dragover",
    (e) => {
        const dt = e.dataTransfer;
        if (!dt) return;
        if (!dt.types || !Array.from(dt.types).includes("Files")) return;
        e.preventDefault();
    },
    true
);

/**
 * input[type=file] 선택 업로드 차단 (동기 캐시 기반)
 */
document.addEventListener(
    "change",
    (e) => {
        const el = e.target;
        if (!(el instanceof HTMLInputElement)) return;
        if (el.type !== "file") return;

        const file = el.files && el.files[0];
        if (!file) return;

        const ext = normalizeExt(file.name);
        if (!ext) return;

        // 캐시 없으면 fail-open + 백그라운드 갱신
        const policy = getCachedPolicyIfFresh();
        if (!policy) {
            refreshPolicy();
            return;
        }

        const blocked = isBlocked(ext, policy);
        if (!blocked) return;

        blockEventHard(e);
        warn("blocked upload (input):", ext, file.name);

        // 파일 선택 상태 자체를 깨트림
        hardResetFileInput(el);

        showToast(`.${ext} 파일은 업로드할 수 없습니다. (정책 차단)`);
    },
    true
);

// 설정/정책 바뀌면 캐시 무효화하고 즉시 갱신
chrome.storage.onChanged.addListener((changes, area) => {
    if (area !== "sync") return;
    if (changes[KEY_TENANT_ID] || changes[KEY_BASE_URL]) {
        cachedPolicy = null;
        cachedAt = 0;
        refreshPolicy();
    }
});

// 최초 로딩 시점에 정책 프리페치 (중요: 이벤트 전에 캐시를 만들어두기)
refreshPolicy();
