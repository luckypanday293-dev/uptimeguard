import { useEffect, useMemo, useState } from 'react'

const API = import.meta.env.VITE_API_URL || 'http://localhost:8081/api'

function formatTime(value) {
  if (!value) return 'Never'
  return new Date(value).toLocaleString()
}

function StatusPill({ status }) {
  const normalized = (status || 'UNKNOWN').toLowerCase()
  return <span className={`status status-${normalized}`}>{status || 'UNKNOWN'}</span>
}

export default function App() {
  const [dashboard, setDashboard] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [checking, setChecking] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({
    name: '', url: 'https://example.com', expectedStatus: 200,
    timeoutMs: 3000, retryCount: 2, checkIntervalSeconds: 30, active: true
  })

  async function loadDashboard() {
    try {
      const response = await fetch(`${API}/dashboard`)
      if (!response.ok) throw new Error('Could not load dashboard')
      setDashboard(await response.json())
      setError('')
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadDashboard()
    const timer = setInterval(loadDashboard, 10000)
    return () => clearInterval(timer)
  }, [])

  const uptimeLabel = useMemo(() => {
    if (!dashboard?.monitors?.length) return '0%'
    const avg = dashboard.monitors.reduce((sum, m) => sum + m.uptimePercent, 0) / dashboard.monitors.length
    return `${avg.toFixed(1)}%`
  }, [dashboard])

  async function checkNow(id) {
    setChecking(id)
    try {
      const response = await fetch(`${API}/monitors/${id}/check`, { method: 'POST' })
      if (!response.ok) throw new Error('Health check failed to run')
      await loadDashboard()
    } catch (e) {
      setError(e.message)
    } finally {
      setChecking(null)
    }
  }

  async function createMonitor(event) {
    event.preventDefault()
    try {
      const response = await fetch(`${API}/monitors`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...form, expectedStatus: Number(form.expectedStatus), timeoutMs: Number(form.timeoutMs), retryCount: Number(form.retryCount), checkIntervalSeconds: Number(form.checkIntervalSeconds) })
      })
      if (!response.ok) {
        const body = await response.json().catch(() => ({}))
        throw new Error(body.error || 'Could not create monitor')
      }
      setShowForm(false)
      setForm({ name: '', url: 'https://example.com', expectedStatus: 200, timeoutMs: 3000, retryCount: 2, checkIntervalSeconds: 30, active: true })
      await loadDashboard()
    } catch (e) {
      setError(e.message)
    }
  }

  if (loading) return <div className="center">Loading UptimeGuard…</div>

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">API MONITORING</p>
          <h1>UptimeGuard</h1>
          <p className="subtitle">Scheduled uptime checks, incident history, and recovery tracking.</p>
        </div>
        <button className="primary" onClick={() => setShowForm(v => !v)}>{showForm ? 'Close' : '+ Add monitor'}</button>
      </header>

      {error && <div className="error-banner">{error}</div>}

      {showForm && (
        <form className="monitor-form" onSubmit={createMonitor}>
          <input placeholder="Monitor name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
          <input placeholder="https://api.example.com/health" value={form.url} onChange={e => setForm({ ...form, url: e.target.value })} required />
          <input type="number" min="100" max="599" value={form.expectedStatus} onChange={e => setForm({ ...form, expectedStatus: e.target.value })} />
          <input type="number" min="250" value={form.timeoutMs} onChange={e => setForm({ ...form, timeoutMs: e.target.value })} />
          <input type="number" min="0" max="5" value={form.retryCount} onChange={e => setForm({ ...form, retryCount: e.target.value })} />
          <input type="number" min="5" value={form.checkIntervalSeconds} onChange={e => setForm({ ...form, checkIntervalSeconds: e.target.value })} />
          <button className="primary" type="submit">Create monitor</button>
        </form>
      )}

      <section className="stats-grid">
        <Stat label="Monitors" value={dashboard.totalMonitors} />
        <Stat label="Currently up" value={dashboard.upMonitors} />
        <Stat label="Currently down" value={dashboard.downMonitors} />
        <Stat label="Open incidents" value={dashboard.openIncidents} />
        <Stat label="Avg. response" value={`${dashboard.averageResponseTimeMs} ms`} />
        <Stat label="Recorded uptime" value={uptimeLabel} />
      </section>

      <section className="panel">
        <div className="panel-heading">
          <div>
            <h2>Monitors</h2>
            <p>Checks run automatically; manual checks are available for testing.</p>
          </div>
          <span className="muted">Refreshes every 10s</span>
        </div>
        <div className="monitor-grid">
          {dashboard.monitors.map(monitor => (
            <article className="monitor-card" key={monitor.id}>
              <div className="card-row">
                <div>
                  <h3>{monitor.name}</h3>
                  <a href={monitor.url} target="_blank" rel="noreferrer">{monitor.url}</a>
                </div>
                <StatusPill status={monitor.currentStatus} />
              </div>
              <div className="metrics">
                <Metric label="Uptime" value={`${monitor.uptimePercent.toFixed(2)}%`} />
                <Metric label="Response" value={monitor.lastResponseTimeMs == null ? '—' : `${monitor.lastResponseTimeMs} ms`} />
                <Metric label="Retries" value={monitor.retryCount} />
                <Metric label="Interval" value={`${monitor.checkIntervalSeconds}s`} />
              </div>
              <div className="card-footer">
                <span>Last check: {formatTime(monitor.lastCheckedAt)}</span>
                <button className="secondary" disabled={checking === monitor.id} onClick={() => checkNow(monitor.id)}>
                  {checking === monitor.id ? 'Checking…' : 'Check now'}
                </button>
              </div>
            </article>
          ))}
        </div>
      </section>

      <div className="two-column">
        <section className="panel">
          <div className="panel-heading"><h2>Incident history</h2></div>
          <div className="list">
            {dashboard.recentIncidents.length === 0 && <p className="empty">No incidents yet.</p>}
            {dashboard.recentIncidents.map(incident => (
              <div className="list-item" key={incident.id}>
                <div>
                  <strong>{incident.monitorName}</strong>
                  <p>{incident.failureMessage || 'Monitor failed a health check.'}</p>
                  <small>{formatTime(incident.startedAt)}</small>
                </div>
                <StatusPill status={incident.status} />
              </div>
            ))}
          </div>
        </section>

        <section className="panel">
          <div className="panel-heading"><h2>Notifications</h2><span className="muted">{dashboard.unreadNotifications} unread</span></div>
          <div className="list">
            {dashboard.recentNotifications.length === 0 && <p className="empty">No notifications yet.</p>}
            {dashboard.recentNotifications.map(notification => (
              <div className="list-item" key={notification.id}>
                <div>
                  <strong>{notification.type === 'RECOVERY' ? 'Recovery' : 'Incident opened'}</strong>
                  <p>{notification.message}</p>
                  <small>{formatTime(notification.createdAt)}</small>
                </div>
                {!notification.read && <span className="unread-dot" title="Unread" />}
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  )
}

function Stat({ label, value }) {
  return <div className="stat"><span>{label}</span><strong>{value}</strong></div>
}

function Metric({ label, value }) {
  return <div><span>{label}</span><strong>{value}</strong></div>
}
