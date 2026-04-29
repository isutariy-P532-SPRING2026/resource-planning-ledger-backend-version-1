/* ---- Tab routing ---- */
document.querySelectorAll('.tab-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-section').forEach(s => s.classList.remove('active'));
    btn.classList.add('active');
    document.getElementById('tab-' + btn.dataset.tab).classList.add('active');
    if (btn.dataset.tab === 'dashboard') loadDashboard();
    if (btn.dataset.tab === 'protocols') loadProtocols();
    if (btn.dataset.tab === 'resources') loadResourceTypes();
    if (btn.dataset.tab === 'plans') loadPlans();
    if (btn.dataset.tab === 'ledger') loadLedgerAccounts();
    if (btn.dataset.tab === 'audit') loadAuditLog();
  });
});

const api = path => fetch('/api' + path).then(r => r.ok ? r.json() : Promise.reject(r));
const post = (path, body) => fetch('/api' + path, {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify(body)
}).then(r => r.ok ? r.json() : r.json().then(e => Promise.reject(e)));

function badge(status) {
  return `<span class="badge badge-${status}">${status.replace('_', ' ')}</span>`;
}

function showError(msg) {
  alert('Error: ' + (msg?.error || msg?.message || JSON.stringify(msg)));
}

/* ---- DASHBOARD ---- */
async function loadDashboard() {
  const accounts = await api('/accounts');
  const pool = accounts.filter(a => a.kind === 'POOL');
  const el = document.getElementById('dashboard-accounts');
  if (!pool.length) { el.innerHTML = '<p class="card-meta">No pool accounts yet.</p>'; return; }
  el.innerHTML = pool.map(a => `
    <div class="card ${a.belowZero ? 'alert' : ''}">
      <div class="card-row">
        <span class="card-title">${a.name}</span>
        <span class="balance-${a.belowZero ? 'negative' : 'ok'}">
          ${a.belowZero ? '⚠ ' : ''}Balance: ${a.balance}
        </span>
      </div>
    </div>`).join('');
}

/* ---- PROTOCOLS ---- */
async function loadProtocols() {
  const protos = await api('/protocols');
  const el = document.getElementById('protocols-list');
  el.innerHTML = protos.map(p => `
    <div class="card">
      <div class="card-row">
        <span class="card-title">${p.name}</span>
        <span class="card-meta">ID: ${p.id}</span>
      </div>
      <div class="card-meta">${p.description}</div>
      ${p.steps.length ? `<div class="card-meta">Steps: ${p.steps.map(s => s.name).join(' → ')}</div>` : ''}
    </div>`).join('') || '<p class="card-meta">No protocols yet.</p>';
}

async function createProtocol() {
  const name = document.getElementById('proto-name').value.trim();
  const description = document.getElementById('proto-desc').value.trim();
  if (!name) return;
  try {
    await post('/protocols', { name, description, steps: [] });
    document.getElementById('proto-name').value = '';
    document.getElementById('proto-desc').value = '';
    loadProtocols();
  } catch(e) { showError(e); }
}

/* ---- RESOURCE TYPES ---- */
async function loadResourceTypes() {
  const rts = await api('/resource-types');
  const el = document.getElementById('resources-list');
  el.innerHTML = rts.map(rt => `
    <div class="card">
      <div class="card-row">
        <span class="card-title">${rt.name}</span>
        <span class="badge badge-${rt.kind === 'ASSET' ? 'SUSPENDED' : 'PROPOSED'}">${rt.kind}</span>
        <span class="card-meta">${rt.unitOfMeasure}</span>
        <span class="card-meta">Pool account: ${rt.poolAccountId}</span>
      </div>
    </div>`).join('') || '<p class="card-meta">No resource types yet.</p>';
}

async function createResourceType() {
  const name = document.getElementById('rt-name').value.trim();
  const kind = document.getElementById('rt-kind').value;
  const unitOfMeasure = document.getElementById('rt-unit').value.trim();
  if (!name || !unitOfMeasure) return;
  try {
    await post('/resource-types', { name, kind, unitOfMeasure });
    document.getElementById('rt-name').value = '';
    document.getElementById('rt-unit').value = '';
    loadResourceTypes();
  } catch(e) { showError(e); }
}

/* ---- PLANS ---- */
async function loadPlans() {
  const plans = await api('/plans');
  const el = document.getElementById('plans-list');
  el.innerHTML = plans.map(p => `
    <div class="card" style="cursor:pointer" onclick="loadPlanDetail(${p.id})">
      <div class="card-row">
        <span class="card-title">${p.name}</span>
        ${badge(p.status)}
        <span class="card-meta">ID: ${p.id} — click to expand</span>
      </div>
    </div>`).join('') || '<p class="card-meta">No plans yet.</p>';
}

async function createPlan() {
  const name = document.getElementById('plan-name').value.trim();
  const protoId = document.getElementById('plan-proto').value.trim();
  const date = document.getElementById('plan-date').value;
  if (!name) return;
  try {
    const plan = await post('/plans', {
      name,
      sourceProtocolId: protoId ? parseInt(protoId) : null,
      targetStartDate: date || null
    });
    document.getElementById('plan-name').value = '';
    document.getElementById('plan-proto').value = '';
    loadPlans();
    loadPlanDetail(plan.id);
  } catch(e) { showError(e); }
}

async function loadPlanDetail(id) {
  _currentPlanId = id;
  const plan = await api(`/plans/${id}`);
  const el = document.getElementById('plan-detail');
  el.innerHTML = `
    <h3>Plan: ${plan.name} ${badge(plan.status)}</h3>
    <div class="tree-node">${renderTree(plan)}</div>
    <button style="margin-top:0.75rem" onclick="loadReport(${id})">View Depth-First Report</button>
    <div id="report-output"></div>`;
}

function renderTree(node) {
  const icon = node.type === 'PLAN' ? '📁' : '📋';
  const children = (node.children || []).map(renderTree).join('');
  const transitions = node.type === 'ACTION' ? renderTransitions(node.id, node.legalTransitions) : '';
  return `
    <div class="tree-node">
      <div class="node-label">
        ${icon} <strong>${node.name}</strong> ${badge(node.status)}
        <span class="card-meta">(ID: ${node.id})</span>
      </div>
      ${transitions}
      ${children ? `<div class="tree-children">${children}</div>` : ''}
    </div>`;
}

// Mapping of event name → {label, cssClass, JS call}.
// Adding a new state in Week 2 only requires adding a new entry here if a new
// event name is introduced (label/style only — backend drives which are enabled).
const EVENT_META = {
  implement: { label: 'Implement', cls: 'btn-implement', fn: id => `implementAction(${id})` },
  complete:  { label: 'Complete',  cls: 'btn-complete',  fn: id => `completeAction(${id})` },
  suspend:   { label: 'Suspend',   cls: 'btn-suspend',   fn: id => `suspendAction(${id})` },
  resume:    { label: 'Resume',    cls: 'btn-resume',    fn: id => `resumeAction(${id})` },
  abandon:   { label: 'Abandon',   cls: 'btn-abandon',   fn: id => `abandonAction(${id})` },
};

function renderTransitions(actionId, legalTransitions) {
  if (!legalTransitions || !legalTransitions.length) return '';
  const btns = legalTransitions.map(event => {
    const meta = EVENT_META[event];
    if (!meta) return `<button onclick="">${event}</button>`; // fallback for unknown events
    return `<button class="${meta.cls}" onclick="${meta.fn(actionId)}">${meta.label}</button>`;
  });
  return `<div class="transition-btns">${btns.join('')}</div>`;
}

async function implementAction(id) {
  const party = prompt('Actual party:') || '';
  const location = prompt('Actual location:') || '';
  try {
    await post(`/actions/${id}/implement`, { actualParty: party, actualLocation: location, actualStart: null });
    refreshCurrentPlan();
  } catch(e) { showError(e); }
}

async function completeAction(id) {
  try {
    await post(`/actions/${id}/complete`, {});
    refreshCurrentPlan();
    loadLedgerAccounts();
  } catch(e) { showError(e); }
}

async function suspendAction(id) {
  const reason = prompt('Suspension reason:') || 'Suspended';
  try {
    await post(`/actions/${id}/suspend`, { reason });
    refreshCurrentPlan();
  } catch(e) { showError(e); }
}

async function resumeAction(id) {
  try {
    await post(`/actions/${id}/resume`, {});
    refreshCurrentPlan();
  } catch(e) { showError(e); }
}

async function abandonAction(id) {
  if (!confirm('Abandon this action?')) return;
  try {
    await post(`/actions/${id}/abandon`, {});
    refreshCurrentPlan();
  } catch(e) { showError(e); }
}

let _currentPlanId = null;
function refreshCurrentPlan() {
  if (_currentPlanId) loadPlanDetail(_currentPlanId);
}

async function loadReport(planId) {
  const report = await api(`/plans/${planId}/report`);
  const el = document.getElementById('report-output');
  el.innerHTML = '<h4 style="margin-top:1rem">Depth-First Report</h4><table><thead><tr>' +
    '<th>Depth</th><th>Name</th><th>Type</th><th>Status</th></tr></thead><tbody>' +
    report.map(n => `<tr>
      <td>${'&nbsp;&nbsp;'.repeat(n.depth)}${n.depth}</td>
      <td>${n.name}</td><td>${n.type}</td><td>${badge(n.status)}</td>
    </tr>`).join('') + '</tbody></table>';
}

/* ---- LEDGER ---- */
async function loadLedgerAccounts() {
  const accounts = await api('/accounts');
  const el = document.getElementById('ledger-accounts');
  el.innerHTML = '<table><thead><tr><th>ID</th><th>Name</th><th>Kind</th><th>Balance</th></tr></thead><tbody>' +
    accounts.map(a => `<tr>
      <td>${a.id}</td>
      <td>${a.name}</td>
      <td>${a.kind}</td>
      <td class="balance-${a.belowZero ? 'negative' : 'ok'}">${a.belowZero ? '⚠ ' : ''}${a.balance}</td>
    </tr>`).join('') + '</tbody></table>';
}

async function loadEntries() {
  const id = document.getElementById('ledger-account-id').value.trim();
  if (!id) return;
  const entries = await api(`/accounts/${id}/entries`);
  const el = document.getElementById('ledger-entries');
  if (!entries.length) { el.innerHTML = '<p class="card-meta">No entries.</p>'; return; }
  el.innerHTML = '<table><thead><tr><th>ID</th><th>Amount</th><th>Charged At</th><th>Booked At</th><th>Action</th><th>Description</th></tr></thead><tbody>' +
    entries.map(e => `<tr>
      <td>${e.id}</td>
      <td class="balance-${e.amount < 0 ? 'negative' : 'ok'}">${e.amount}</td>
      <td>${e.chargedAt?.substring(0, 19).replace('T', ' ')}</td>
      <td>${e.bookedAt?.substring(0, 19).replace('T', ' ')}</td>
      <td>${e.originatingActionId ?? '-'}</td>
      <td>${e.description ?? ''}</td>
    </tr>`).join('') + '</tbody></table>';
}

/* ---- AUDIT LOG ---- */
async function loadAuditLog() {
  const entries = await api('/audit-log');
  const el = document.getElementById('audit-list');
  el.innerHTML = '<table><thead><tr><th>ID</th><th>Event</th><th>Action</th><th>Account</th><th>Timestamp</th><th>Details</th></tr></thead><tbody>' +
    entries.map(e => `<tr>
      <td>${e.id}</td>
      <td><strong>${e.event}</strong></td>
      <td>${e.actionId ?? '-'}</td>
      <td>${e.accountId ?? '-'}</td>
      <td>${e.timestamp?.substring(0, 19).replace('T', ' ')}</td>
      <td>${e.details ?? ''}</td>
    </tr>`).join('') + '</tbody></table>';
}

/* ---- Init ---- */
loadDashboard();
