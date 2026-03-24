const pushState = { streams: [], targets: [], runtimes: new Map() };

const pushBody = document.getElementById("push-targets-body");
const pushStatus = document.getElementById("push-page-status");
const pushModal = document.getElementById("push-modal");
const pushForm = document.getElementById("push-form");
const pushFilter = document.getElementById("push-stream-filter");
const pushStreamSelect = document.getElementById("push-stream-id");

document.getElementById("refresh-pushes").addEventListener("click", loadPushes);
document.getElementById("open-push-modal").addEventListener("click", () => openPushModal());
document.getElementById("reset-push-form").addEventListener("click", resetPushForm);
pushForm.addEventListener("submit", submitPushForm);
pushFilter.addEventListener("change", renderPushes);
document.querySelectorAll("[data-close-push-modal]").forEach((element) => {
  element.addEventListener("click", closePushModal);
});

loadPushes();

async function loadPushes() {
  try {
    setPushStatus("Syncing", false);
    const [streams, targets, runtimes] = await Promise.all([
      api("/api/admin/streams"),
      api("/api/admin/push-targets"),
      api("/api/local/push-targets"),
    ]);
    pushState.streams = streams;
    pushState.targets = targets;
    pushState.runtimes = new Map(runtimes.map((item) => [item.targetCode, item]));
    renderStreamOptions();
    renderPushes();
    setPushStatus(`Ready ${new Date().toLocaleTimeString()}`, false);
  } catch (error) {
    setPushStatus(`Failed: ${error.message}`, true);
    window.alert(error.message);
  }
}

function renderStreamOptions() {
  const options = pushState.streams.map((stream) => `<option value="${escapeHtml(stream.id)}">${escapeHtml(stream.streamCode)} - ${escapeHtml(stream.name)}</option>`).join("");
  pushFilter.innerHTML = '<option value="">All streams</option>' + options;
  pushStreamSelect.innerHTML = options;
}

function renderPushes() {
  const streamById = new Map(pushState.streams.map((stream) => [String(stream.id), stream]));
  const filterValue = pushFilter.value;
  const filteredTargets = pushState.targets.filter((target) => !filterValue || String(target.streamId) === filterValue);
  pushBody.innerHTML = filteredTargets.map((target) => {
    const runtime = pushState.runtimes.get(target.targetCode);
    const stream = streamById.get(String(target.streamId));
    return `
      <tr>
        <td>${escapeHtml(target.targetCode)}</td>
        <td>${escapeHtml(stream?.streamCode || "-")}</td>
        <td><span class="chip">${escapeHtml(target.targetProtocol)}</span></td>
        <td>${renderStateChip(target.expectedState)}</td>
        <td>${renderStateChip(runtime?.pushStatus || "IDLE")}</td>
        <td class="${runtime?.lastError ? "error-text" : "muted"}">${escapeHtml(runtime?.lastError || "-")}</td>
        <td>
          <div class="actions-inline">
            <button class="button" data-action="start" data-target-code="${escapeHtml(target.targetCode)}">Start</button>
            <button class="button" data-action="stop" data-target-code="${escapeHtml(target.targetCode)}">Stop</button>
            <button class="button" data-action="edit" data-target-code="${escapeHtml(target.targetCode)}">Edit</button>
            <button class="button button-danger" data-action="delete" data-target-code="${escapeHtml(target.targetCode)}">Delete</button>
          </div>
        </td>
      </tr>
    `;
  }).join("");

  pushBody.querySelectorAll("button[data-action]").forEach((button) => {
    button.addEventListener("click", handlePushAction);
  });
}

async function handlePushAction(event) {
  const action = event.currentTarget.dataset.action;
  const targetCode = event.currentTarget.dataset.targetCode;
  if (action === "start" || action === "stop") {
    await api(`/api/local/push-targets/${encodeURIComponent(targetCode)}/${action}`, { method: "POST" });
    await loadPushes();
    return;
  }
  if (action === "edit") {
    const target = pushState.targets.find((item) => item.targetCode === targetCode);
    openPushModal(target);
    return;
  }
  if (action === "delete") {
    if (!window.confirm(`Delete push target ${targetCode}?`)) return;
    await api(`/api/admin/push-targets/${encodeURIComponent(targetCode)}`, { method: "DELETE" });
    await loadPushes();
  }
}

function openPushModal(target = null) {
  document.getElementById("push-modal-title").textContent = target ? `Edit ${target.targetCode}` : "New push target";
  resetPushForm();
  if (target) {
    document.getElementById("push-edit-code").value = target.targetCode;
    document.getElementById("push-stream-id").value = String(target.streamId);
    document.getElementById("push-target-code").value = target.targetCode || "";
    document.getElementById("push-target-name").value = target.targetName || "";
    document.getElementById("push-target-type").value = target.targetType || "CLOUD_ZLM";
    document.getElementById("push-target-protocol").value = target.targetProtocol || "RTMP";
    document.getElementById("push-target-url").value = target.targetUrl || "";
    document.getElementById("push-target-app").value = target.targetApp || "";
    document.getElementById("push-target-stream").value = target.targetStream || "";
    document.getElementById("push-target-enabled").value = String(target.enabled ?? 1);
    document.getElementById("push-target-expected-state").value = target.expectedState || "STOPPED";
    document.getElementById("push-target-remark").value = target.remark || "";
  }
  pushModal.classList.remove("hidden");
}

function closePushModal() {
  pushModal.classList.add("hidden");
}

function resetPushForm() {
  pushForm.reset();
  document.getElementById("push-edit-code").value = "";
  document.getElementById("push-target-type").value = "CLOUD_ZLM";
  document.getElementById("push-target-protocol").value = "RTMP";
  document.getElementById("push-target-enabled").value = "1";
  document.getElementById("push-target-expected-state").value = "STOPPED";
  if (pushState.streams.length > 0) {
    document.getElementById("push-stream-id").value = String(pushState.streams[0].id);
  }
}

async function submitPushForm(event) {
  event.preventDefault();
  const editCode = document.getElementById("push-edit-code").value;
  const payload = {
    streamId: Number(document.getElementById("push-stream-id").value),
    targetCode: document.getElementById("push-target-code").value.trim(),
    targetName: document.getElementById("push-target-name").value.trim(),
    targetType: document.getElementById("push-target-type").value.trim(),
    targetProtocol: document.getElementById("push-target-protocol").value,
    targetUrl: document.getElementById("push-target-url").value.trim(),
    targetApp: document.getElementById("push-target-app").value.trim(),
    targetStream: document.getElementById("push-target-stream").value.trim(),
    enabled: Number(document.getElementById("push-target-enabled").value),
    expectedState: document.getElementById("push-target-expected-state").value,
    remark: document.getElementById("push-target-remark").value.trim(),
  };
  await api(editCode ? `/api/admin/push-targets/${encodeURIComponent(editCode)}` : "/api/admin/push-targets", {
    method: editCode ? "PUT" : "POST",
    body: JSON.stringify(payload),
  });
  closePushModal();
  await loadPushes();
}

function renderStateChip(value) {
  const state = value || "UNKNOWN";
  let cls = "chip";
  if (state === "RUNNING") cls += " chip-running";
  if (state === "STOPPED" || state === "IDLE" || state === "FAILED") cls += " chip-stopped";
  if (state === "STARTING" || state === "STOPPING") cls += " chip-starting";
  return `<span class="${cls}">${escapeHtml(state)}</span>`;
}

function setPushStatus(message, failed) {
  pushStatus.textContent = message;
  pushStatus.className = `status-badge${failed ? " chip-stopped" : ""}`;
}

async function api(url, options = {}) {
  const response = await fetch(url, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options,
  });
  if (!response.ok) {
    let message = `Request failed: ${response.status}`;
    try {
      const body = await response.json();
      message = body.message || message;
    } catch (error) {
      message = response.statusText || message;
    }
    throw new Error(message);
  }
  if (response.status === 204) return null;
  return response.json();
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}