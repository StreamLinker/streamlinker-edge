const pullState = { streams: [], runtimes: new Map() };

const pullBody = document.getElementById("pull-streams-body");
const pullStatus = document.getElementById("pull-page-status");
const streamModal = document.getElementById("stream-modal");
const streamForm = document.getElementById("stream-form");

document.getElementById("refresh-pulls").addEventListener("click", loadPulls);
document.getElementById("open-stream-modal").addEventListener("click", () => openStreamModal());
document.getElementById("reset-stream-form").addEventListener("click", resetStreamForm);
streamForm.addEventListener("submit", submitStreamForm);
document.querySelectorAll("[data-close-stream-modal]").forEach((element) => {
  element.addEventListener("click", closeStreamModal);
});

loadPulls();

async function loadPulls() {
  try {
    setPullStatus("Syncing", false);
    const [streams, runtimes] = await Promise.all([
      api("/api/admin/streams"),
      api("/api/local/streams"),
    ]);
    pullState.streams = streams;
    pullState.runtimes = new Map(runtimes.map((item) => [item.streamId, item]));
    renderPulls();
    setPullStatus(`Ready ${new Date().toLocaleTimeString()}`, false);
  } catch (error) {
    setPullStatus(`Failed: ${error.message}`, true);
    window.alert(error.message);
  }
}

function renderPulls() {
  pullBody.innerHTML = pullState.streams.map((stream) => {
    const runtime = pullState.runtimes.get(stream.streamCode);
    return `
      <tr>
        <td>${escapeHtml(stream.streamCode)}</td>
        <td>${escapeHtml(stream.name)}</td>
        <td><span class="chip">${escapeHtml(stream.accessMode)}</span></td>
        <td>${renderStateChip(stream.expectedState)}</td>
        <td>${renderStateChip(runtime?.state || "IDLE")}</td>
        <td class="${runtime?.lastError ? "error-text" : "muted"}">${escapeHtml(runtime?.lastError || "-")}</td>
        <td>
          <div class="actions-inline">
            <button class="button" data-action="start" data-stream-code="${escapeHtml(stream.streamCode)}">Start</button>
            <button class="button" data-action="stop" data-stream-code="${escapeHtml(stream.streamCode)}">Stop</button>
            <button class="button" data-action="edit" data-stream-code="${escapeHtml(stream.streamCode)}">Edit</button>
            <button class="button button-danger" data-action="delete" data-stream-code="${escapeHtml(stream.streamCode)}">Delete</button>
          </div>
        </td>
      </tr>
    `;
  }).join("");

  pullBody.querySelectorAll("button[data-action]").forEach((button) => {
    button.addEventListener("click", handlePullAction);
  });
}

async function handlePullAction(event) {
  const action = event.currentTarget.dataset.action;
  const streamCode = event.currentTarget.dataset.streamCode;
  if (action === "start" || action === "stop") {
    await api(`/api/local/streams/${encodeURIComponent(streamCode)}/${action}`, { method: "POST" });
    await loadPulls();
    return;
  }
  if (action === "edit") {
    const stream = pullState.streams.find((item) => item.streamCode === streamCode);
    openStreamModal(stream);
    return;
  }
  if (action === "delete") {
    if (!window.confirm(`Delete stream ${streamCode}?`)) return;
    await api(`/api/admin/streams/${encodeURIComponent(streamCode)}`, { method: "DELETE" });
    await loadPulls();
  }
}

function openStreamModal(stream = null) {
  document.getElementById("stream-modal-title").textContent = stream ? `Edit ${stream.streamCode}` : "New stream";
  resetStreamForm();
  if (stream) {
    document.getElementById("stream-edit-code").value = stream.streamCode;
    document.getElementById("stream-code").value = stream.streamCode;
    document.getElementById("stream-name").value = stream.name || "";
    document.getElementById("stream-source-url").value = stream.sourceUrl || "";
    document.getElementById("stream-source-protocol").value = stream.sourceProtocol || "RTSP";
    document.getElementById("stream-access-mode").value = stream.accessMode || "FFMPEG";
    document.getElementById("stream-local-app").value = stream.localApp || "live";
    document.getElementById("stream-local-stream").value = stream.localStream || "";
    document.getElementById("stream-enabled").value = String(stream.enabled ?? 1);
    document.getElementById("stream-expected-state").value = stream.expectedState || "STOPPED";
    document.getElementById("stream-remark").value = stream.remark || "";
  }
  streamModal.classList.remove("hidden");
}

function closeStreamModal() {
  streamModal.classList.add("hidden");
}

function resetStreamForm() {
  streamForm.reset();
  document.getElementById("stream-edit-code").value = "";
  document.getElementById("stream-source-protocol").value = "RTSP";
  document.getElementById("stream-access-mode").value = "FFMPEG";
  document.getElementById("stream-local-app").value = "live";
  document.getElementById("stream-enabled").value = "1";
  document.getElementById("stream-expected-state").value = "STOPPED";
}

async function submitStreamForm(event) {
  event.preventDefault();
  const editCode = document.getElementById("stream-edit-code").value;
  const payload = {
    streamCode: document.getElementById("stream-code").value.trim(),
    name: document.getElementById("stream-name").value.trim(),
    sourceUrl: document.getElementById("stream-source-url").value.trim(),
    sourceProtocol: document.getElementById("stream-source-protocol").value.trim(),
    accessMode: document.getElementById("stream-access-mode").value,
    localApp: document.getElementById("stream-local-app").value.trim(),
    localStream: document.getElementById("stream-local-stream").value.trim(),
    enabled: Number(document.getElementById("stream-enabled").value),
    expectedState: document.getElementById("stream-expected-state").value,
    remark: document.getElementById("stream-remark").value.trim(),
  };
  await api(editCode ? `/api/admin/streams/${encodeURIComponent(editCode)}` : "/api/admin/streams", {
    method: editCode ? "PUT" : "POST",
    body: JSON.stringify(payload),
  });
  closeStreamModal();
  await loadPulls();
}

function renderStateChip(value) {
  const state = value || "UNKNOWN";
  let cls = "chip";
  if (state === "RUNNING") cls += " chip-running";
  if (state === "STOPPED" || state === "IDLE" || state === "FAILED") cls += " chip-stopped";
  if (state === "STARTING" || state === "STOPPING") cls += " chip-starting";
  return `<span class="${cls}">${escapeHtml(state)}</span>`;
}

function setPullStatus(message, failed) {
  pullStatus.textContent = message;
  pullStatus.className = `status-badge${failed ? " chip-stopped" : ""}`;
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