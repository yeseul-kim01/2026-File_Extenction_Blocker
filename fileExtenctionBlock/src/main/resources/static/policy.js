const KEY = "ks.tenantId";
const tenantId = localStorage.getItem(KEY);

if (!tenantId) {
    alert("Tenant ID가 없습니다. 먼저 입력 페이지로 이동합니다.");
    location.href = "/";
}

const $tenantLabel = document.getElementById("tenantLabel");
const $fixedBox = document.getElementById("fixedBox");

const $customInput = document.getElementById("customInput");
const $addBtn = document.getElementById("addBtn");

const $customTags = document.getElementById("customTags");
const $countNow = document.getElementById("countNow");
const $countMax = document.getElementById("countMax");

const $reloadBtn = document.getElementById("reloadBtn");
const $toast = document.getElementById("toast");

$tenantLabel.textContent = tenantId;

function showToast(msg) {
    $toast.textContent = msg;
    $toast.classList.remove("hidden");
    setTimeout(() => $toast.classList.add("hidden"), 2000);
}

function normalizeExt(s) {
    return (s || "").trim().toLowerCase().replace(/^\./, "");
}

async function apiFetch(url, options = {}) {
    const res = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            "X-Tenant-Id": tenantId,
            ...(options.headers || {}),
        },
    });

    if (res.status === 204) return null;

    const contentType = res.headers.get("content-type") || "";
    const body = contentType.includes("application/json") ? await res.json() : await res.text();

    if (!res.ok) {
        const msg =
            (body && body.message) ? `${body.code ?? res.status} - ${body.message}` :
                (typeof body === "string" && body) ? body :
                    `HTTP ${res.status}`;
        throw new Error(msg);
    }

    return body;
}

function renderFixed(fixedList) {
    $fixedBox.innerHTML = "";

    fixedList.forEach(item => {
        const ext = item.ext;
        const checked = !!item.blocked; // blocked=true이면 체크된 상태로 표현 (차단)

        const wrap = document.createElement("label");
        wrap.className = "check-item";

        const cb = document.createElement("input");
        cb.type = "checkbox";
        cb.checked = checked;

        cb.addEventListener("change", async () => {
            try {
                // PUT 방식: { ext, blocked }
                await apiFetch("/api/policy/extensions/fixed", {
                    method: "PUT",
                    body: JSON.stringify({ ext, blocked: cb.checked }),
                });
                showToast(`.${ext} ${cb.checked ? "차단" : "허용"} 적용`);
            } catch (e) {
                cb.checked = !cb.checked; // 롤백
                showToast(e.message);
            }
        });

        const text = document.createElement("span");
        text.textContent = ext;

        wrap.appendChild(cb);
        wrap.appendChild(text);
        $fixedBox.appendChild(wrap);
    });
}

function renderCustom(customList, limit) {
    $customTags.innerHTML = "";
    $countMax.textContent = String(limit ?? 200);

    const arr = (customList || []).map(v => (typeof v === "string" ? v : v.ext));
    $countNow.textContent = String(arr.length);

    arr.forEach(ext => {
        const tag = document.createElement("div");
        tag.className = "tag";

        const t = document.createElement("span");
        t.className = "ext";
        t.textContent = ext;

        const x = document.createElement("button");
        x.className = "x";
        x.type = "button";
        x.textContent = "×";

        x.addEventListener("click", async () => {
            try {
                await apiFetch(`/api/policy/extensions/custom/${encodeURIComponent(ext)}`, { method: "DELETE" });
                showToast(`.${ext} 삭제`);
                await load();
            } catch (e) {
                showToast(e.message);
            }
        });

        tag.appendChild(t);
        tag.appendChild(x);
        $customTags.appendChild(tag);
    });
}

async function load() {
    try {
        const snapshot = await apiFetch("/api/policy/extensions", { method: "GET" });
        renderFixed(snapshot.fixed || []);
        renderCustom(snapshot.custom || [], snapshot.limit ?? 200);
    } catch (e) {
        showToast(e.message);
    }
}

$addBtn.addEventListener("click", async () => {
    const ext = normalizeExt($customInput.value);
    if (!ext) return showToast("확장자를 입력하세요.");

    if (ext.length > 20) return showToast("확장자는 최대 20자까지 입력 가능합니다.");

    try {
        await apiFetch("/api/policy/extensions/custom", {
            method: "POST",
            body: JSON.stringify({ ext }),
        });
        $customInput.value = "";
        showToast(`.${ext} 추가`);
        await load();
    } catch (e) {
        showToast(e.message);
    }
});

$customInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") $addBtn.click();
});

$reloadBtn.addEventListener("click", load);

load();


