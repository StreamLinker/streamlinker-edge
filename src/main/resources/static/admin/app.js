const state = {
  streams: [],
  streamRuntimeMap: new Map(),
  pushTargets: [],
  pushRuntimeMap: new Map(),
  selectedStreamCode: null,
};

const elements = {
  streamTableBody: document.getElementById("stream-table-body"),
  pushTargetTableBody: document.getElementById("push-target-table-body"),
  selectedStreamLabel: document.getElementById("selected-stream-label"),
  selectedPushStreamLabel: document.getElementById("selected-push-stream-label"),
  statusBadge: document.getElementById("status-badge"),
  refreshButton: document.getElementById("refresh-dashboard"),
  newStreamButton: document.getElementById("new-stream"),
  newPushTargetButton: document.getElementById("new-push-target"),
  streamForm: document.getElementById("stream-form"),
  pushTargetForm: document.getElementById("push-target-form"),
};

document.addEventListener("DOMContentLoaded", () => {
  bindEvents();
  bootstrap();
});

function bindEvents() {
  elements.refreshButton.addEventListener("click", bootstrap);
  elements.newStreamButton.addEventListener("click", () => resetStreamForm());
  elements.newPushTargetButton.addEventListener("click", () => resetPushTargetForm(true));
  document.getElementById("reset-stream-form").addEventListener("click", () => resetStreamForm());
  document.getElementById("reset-push-target-form").addEventListener("click", () => resetPushTargetForm(true));
  elements.streamForm.addEventListener("submit", submitStreamForm);
  elements.pushTargetForm.addEventListener("submit", submitPushTargetForm);
}

async function bootstrap() {
  try {
    setStatus("正在同步", false);
    await Promise.all([loadStreams(), loadPushTargets()]);
    if (!state.selectedStreamCode && state.streams.length > 0) {
      state.selectedStreamCode = state.streams[0].streamCode;
    }
    renderStreams();
    renderPushTargets();
    syncSelectionLabels();
    setStatus(`已连接 · ${new Date().toLocaleTimeString()}`, false);
  } catch (error) {
    setStatus(`连接失败 · ${error.message}`, true);
    window.alert(error.message);
  }
}

async function loadStreams() {
  const [streams, runtimes] = await Promise.all([
    api("/api/admin/streams"),
    api("/api/local/streams"),
  ]);
  state.streams = streams;
  state.streamRuntimeMap = new Map(runtimes.map((item) => [item.streamId, item]));
}

async function loadPushTargets() {
  const [targets, runtimes] = await Promise.all([
    api("/api/admin/push-targets"),
    api("/api/local/push-targets"),
  ]);
  state.pushTargets = targets;
  state.pushRuntimeMap = new Map(runtimes.map((item) => [item.targetCode, item]));
}

function renderStreams() {
  elements.streamTableBody.innerHTML = "";
  for (const stream of state.streams) {
    const runtime = state.streamRuntimeMap.get(stream.streamCode);
    const row = document.createElement("tr");
    row.className = stream.streamCode === state.selectedStreamCode ? "is-selected" : "";
    row.innerHTML = `
      <td><button class="button-link" data-action="select-stream" data-stream-code="${escapeHtml(stream.streamCode)}">${escapeHtml(stream.streamCode)}</button></td>
      <td>${escapeHtml(stream.name)}</td>
      <td><span class="chip">${escapeHtml(stream.accessMode)}</span></td>
      <td>${renderStateChip(stream.expectedState)}</td>
      <td>${renderStateChip(runtime?.state || "IDLE")}</td>
      <td class="${runtime?.lastError ? "error-text" : "muted"}">${escapeHtml(runtime?.lastError || "-")}</td>
      <td>
        <div class="actions-inline">
          <button class="button" data-action="stream-start" data-stream-code="${escapeHtml(stream.streamCode)}">启动</button>
          <button class="button" data-action="stream-stop" data-stream-code="${escapeHtml(stream.streamCode)}">停止</button>
          <button class="button" data-action="stream-edit" data-stream-code="${escapeHtml(stream.streamCode)}">编辑</button>
          <button class="button button-danger" data-action="stream-delete" data-stream-code="${escapeHtml(stream.streamCode)}">删除</button>
        </div>
      </td>
    `;
    elements.streamTableBody.appendChild(row);
  }

  elements.streamTableBody.querySelectorAll("button[data-action]").forEach((button) => {
    button.addEventListener("click", handleStreamAction);
  });
}

function renderPushTargets() {
  elements.pushTargetTableBody.innerHTML = "";
  const stream = selectedStream();
  const selectedTargets = state.pushTargets.filter((item) => item.streamId === stream?.id);
  for (const target of selectedTargets) {
    const runtime = state.pushRuntimeMap.get(target.targetCode);
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${escapeHtml(target.targetCode)}</td>
      <td>${escapeHtml(target.targetName)}</td>
      <td><span class="chip">${escapeHtml(target.targetProtocol)}</span></td>
      <td>${renderStateChip(target.expectedState)}</td>
      <td>${renderStateChip(runtime?.pushStatus || "IDLE")}</td>
      <td class="${runtime?.lastError ? "error-text" : "muted"}">${escapeHtml(runtime?.lastError || "-")}</td>
      <td>
        <div class="actions-inline">
          <button class="button" data-action="push-start" data-target-code="${escapeHtml(target.targetCode)}">启动</button>
          <button class="button" data-action="push-stop" data-target-code="${escapeHtml(target.targetCode)}">停止</button>
          <button class="button" data-action="push-edit" data-target-code="${escapeHtml(target.targetCode)}">编辑</button>
          <button class="button button-danger" data-action="push-delete" data-target-code="${escapeHtml(target.targetCode)}">删除</button>
        </div>
      </td>
    `;
    elements.pushTargetTableBody.appendChild(row);
  }

  elements.pushTargetTableBody.querySelectorAll("button[data-action]").forEach((button) => {
    button.addEventListener("click", handlePushAction);
  });
}

function selectedStream() {
  return state.streams.find((item) => item.streamCode === state.selectedStreamCode) || null;
}

function syncSelectionLabels() {
  const stream = selectedStream();
  if (!stream) {
    elements.selectedStreamLabel.textContent = "当前未选中流";
    elements.selectedPushStreamLabel.textContent = "请先选中一条流";
    elements.newPushTargetButton.disabled = true;
    return;
  }
  elements.selectedStreamLabel.textContent = `当前选中: ${stream.streamCode} · ${stream.name}`;
  elements.selectedPushStreamLabel.textContent = `推流目标挂在流 ${stream.streamCode} 下`;
  elements.newPushTargetButton.disabled = false;
}

function fillStreamForm(stream) {
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

function resetStreamForm() {
  document.getElementById("stream-edit-code").value = "";
  elements.streamForm.reset();
  document.getElementById("stream-source-protocol").value = "RTSP";
  document.getElementById("stream-access-mode").value = "FFMPEG";
  document.getElementById("stream-local-app").value = "live";
  document.getElementById("stream-enabled").value = "1";
  document.getElementById("stream-expected-state").value = "STOPPED";
}

function fillPushTargetForm(target) {
  document.getElementById("push-target-edit-code").value = target.targetCode;
  document.getElementById("push-stream-id").value = target.streamId ?? "";
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

function resetPushTargetForm(prefillSelectedStream = false) {
  document.getElementById("push-target-edit-code").value = "";
  elements.pushTargetForm.reset();
  document.getElementById("push-target-type").value = "CLOUD_ZLM";
  document.getElementById("push-target-protocol").value = "RTMP";
  document.getElementById("push-target-enabled").value = "1";
  document.getElementById("push-target-expected-state").value = "STOPPED";
  const stream = selectedStream();
  document.getElementById("push-stream-id").value = prefillSelectedStream && stream ? String(stream.id) : "";
}

async function submitStreamForm(event) {
  event.preventDefault();
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
  const editCode = document.getElementById("stream-edit-code").value;
  await api(editCode ? `/api/admin/streams/${encodeURIComponent(editCode)}` : "/api/admin/streams", {
    method: editCode ? "PUT" : "POST",
    body: JSON.stringify(payload),
  });
  state.selectedStreamCode = payload.streamCode;
  await bootstrap();
  fillStreamForm(payload);
}

async function submitPushTargetForm(event) {
  event.preventDefault();
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
  const editCode = document.getElementById("push-target-edit-code").value;
  await api(editCode ? `/api/admin/push-targets/${encodeURIComponent(editCode)}` : "/api/admin/push-targets", {
    method: editCode ? "PUT" : "POST",
    body: JSON.stringify(payload),
  });
  await bootstrap();
}

async function handleStreamAction(event) {
  const action = event.currentTarget.dataset.action;
  const streamCode = event.currentTarget.dataset.streamCode;
  if (action === "select-stream") {
    state.selectedStreamCode = streamCode;
    renderStreams();
    renderPushTargets();
    syncSelectionLabels();
    resetPushTargetForm(true);
    return;
  }
  if (action === "stream-edit") {
    const stream = state.streams.find((item) => item.streamCode === streamCode);
    if (stream) {
      fillStreamForm(stream);
    }
    return;
  }
  if (action === "stream-delete") {
    if (!window.confirm(`确认删除流 ${streamCode} 吗？`)) {
      return;
    }
    await api(`/api/admin/streams/${encodeURIComponent(streamCode)}`, { method: "DELETE" });
    if (state.selectedStreamCode === streamCode) {
      state.selectedStreamCode = null;
    }
    await bootstrap();
    return;
  }
  if (action === "stream-start" || action === "stream-stop") {
    await api(`/api/local/streams/${encodeURIComponent(streamCode)}/${action === "stream-start" ? "start" : "stop"}`, {
      method: "POST",
    });
    await bootstrap();
  }
}

async function handlePushAction(event) {
  const action = event.currentTarget.dataset.action;
  const targetCode = event.currentTarget.dataset.targetCode;
  if (action === "push-edit") {
    const target = state.pushTargets.find((item) => item.targetCode === targetCode);
    if (target) {
      fillPushTargetForm(target);
    }
    return;
  }
  if (action === "push-delete") {
    if (!window.confirm(`确认删除推流目标 ${targetCode} 吗？`)) {
      return;
    }
    await api(`/api/admin/push-targets/${encodeURIComponent(targetCode)}`, { method: "DELETE" });
    await bootstrap();
    return;
  }
  if (action === "push-start" || action === "push-stop") {
    await api(`/api/local/push-targets/${encodeURIComponent(targetCode)}/${action === "push-start" ? "start" : "stop"}`, {
      method: "POST",
    });
    await bootstrap();
  }
}

async function api(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
    ...options,
  });
  if (!response.ok) {
    let message = `Request failed: ${response.status}`;
    try {
      const payload = await response.json();
      message = payload.message || message;
    } catch (error) {
      message = response.statusText || message;
    }
    throw new Error(message);
  }
  if (response.status === 204) {
    return null;
  }
  return response.json();
}

function renderStateChip(stateValue) {
  const value = stateValue || "UNKNOWN";
  let cls = "chip";
  if (value === "RUNNING") {
    cls += " chip-running";
  } else if (value === "STOPPED" || value === "IDLE" || value === "FAILED") {
    cls += " chip-stopped";
  } else if (value === "STARTING" || value === "STOPPING") {
    cls += " chip-starting";
  }
  return `<span class="${cls}">${escapeHtml(value)}</span>`;
}

function setStatus(message, isError) {
  elements.statusBadge.textContent = message;
  elements.statusBadge.className = `status-badge${isError ? " chip-stopped" : ""}`;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}