import Icon from './Icon.jsx'

export default function StatCard({ label, value, icon, accent }) {
  return <article className={`stat-card stat-${accent}`}><div className="stat-top"><div className="stat-icon"><Icon name={icon} size={20} /></div></div><div className="stat-label">{label}</div><div className="stat-value">{value ?? '—'}</div></article>
}
