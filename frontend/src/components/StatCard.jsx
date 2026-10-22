import Icon from './Icon.jsx'

export default function StatCard({ label, value, icon, accent, note }) {
  return <article className={`stat-card stat-${accent}`}><div className="stat-top"><div className="stat-icon"><Icon name={icon} size={20} /></div><span className="stat-note">{note}</span></div><div className="stat-label">{label}</div><div className="stat-value">{value ?? '—'}</div></article>
}
