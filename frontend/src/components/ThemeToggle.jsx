import Icon from './Icon.jsx'

export default function ThemeToggle({ theme, onToggle }) {
  const dark = theme === 'dark'
  const action = dark ? 'Chuyển sang giao diện sáng' : 'Chuyển sang giao diện tối'
  return (
    <button type="button" className="button button-secondary theme-toggle"
      aria-label="Giao diện tối" aria-pressed={dark} title={action} onClick={onToggle}>
      <Icon name={dark ? 'sun' : 'moon'} size={17}/>
      <span>{dark ? 'Chế độ sáng' : 'Chế độ tối'}</span>
    </button>
  )
}
