const badge = document.getElementById("status-badge");
const statsGrid = document.getElementById("stats-grid");
const streamsBody = document.getElementById("dashboard-streams-body");
const pushesBody = document.getElementById("dashboard-pushes-body");

document.getElementById("refresh-dashboard").addEventListener("click", loadDashboard);
loadDashboard();

async function loadDashboard() {
  try {
    setBadge("Syncing", false);
    const [streams, runtimes, pushTargets, pushRuntimes] = await Promise.all([
      api("/api/admin/streams"),
      api("/api/local/streams"),
      api("/api/admin/push-targets"),
      api("/api/local/push-targets"),
    ]);

    const streamRuntimeMap = new Map(runtimes.map((item) => [item.streamId, item]));
    const pushRuntimeMap = new Map(pushRuntimes.map((item) => [item.targetCode, item]));

    renderStats(streams, runtimes, pushTargets, pushRuntimes);
    renderStreamTable(streams, streamRuntimeMap);
    renderPushTable(pushTargets, pushRuntimeMap, streams);
    setBadge(`Ready ${new Date().toLocaleTimeString()}`, false);
  } catch (error) {
    setBadge(`Failed: ${error.message}`, true);
    window.alert(error.message);
  }
}

function renderStats(streams, runtimes, pushTargets, pushRuntimes) {
  const streamRunning = runtimes.filter((item) => item.state === "RUNNING").length;
  const pushRunning = pushRuntimes.filter((item) => item.pushStatus === "RUNNING").length;
  const expectedRunning = streams.filter((item) => item.expectedState === "RUNNING").length;
  const expectedPushRunning = pushTargets.filter((item) => item.expectedState === "RUNNING").length;
  statsGrid.innerHTML = [
    statCard("Streams", streams.length),
    statCard("Streams running", streamRunning),
    statCard("Push targets", pushTargets.length),
    statCard("Push running", pushRunning),
    statCard("Expected stream run", expectedRunning),
    statCard("Expected push run", expectedPushRunning),
  ].join("");
}

function renderStreamTable(streams, runtimeMap) {
  streamsBody.innerHTML = streams.map((stream) => {
    const runtime = runtimeMap.get(stream.streamCode);
    return `
      <tr>
        <td>${escapeHtml(stream.streamCode)}</td>
        <td>${renderStateChip(stream.expectedState)}</td>
        <td>${renderStateChip(runtime?.state || "IDLE")}</td>
        <td class="${runtime?.lastError ? "error-text" : "muted"}">${escapeHtml(runtime?.lastError || "-")}</td>
      </tr>
    `;
  }).join("");
}

function renderPushTable(pushTargets, runtimeMap, streams) {
  const streamById = new Map(streams.map((stream) => [stream.id, stream]));
  pushesBody.innerHTML = pushTargets.map((target) => {
    const runtime = runtimeMap.get(target.targetCode);
    const stream = streamById.get(target.streamId);
    return `
      <tr>
        <td>${escapeHtml(target.targetCode)}<div class="muted">${escapeHtml(stream?.streamCode || "-")}</div></td>
        <td>${escapeHtml(target.targetProtocol)}</td>
        <td>${renderStateChip(target.expectedState)}</td>
        <td>${renderStateChip(runtime?.pushStatus || "IDLE")}</td>
      </tr>
    `;
  }).join("");
}

function statCard(label, value) {
  return `<div class="stat-card"><p>${escapeHtml(label)}</p><strong>${escapeHtml(value)}</strong></div>`;
}

function renderStateChip(value) {
  const state = value || "UNKNOWN";
  let cls = "chip";
  if (state === "RUNNING") cls += " chip-running";
  if (state === "STOPPED" || state === "IDLE" || state === "FAILED") cls += " chip-stopped";
  if (state === "STARTING" || state === "STOPPING") cls += " chip-starting";
  return `<span class="${cls}">${escapeHtml(state)}</span>`;
}

function setBadge(message, failed) {
  badge.textContent = message;
  badge.className = `status-badge${failed ? " chip-stopped" : ""}`;
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