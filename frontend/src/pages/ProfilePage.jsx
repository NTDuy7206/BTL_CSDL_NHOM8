import { useEffect, useRef, useState } from 'react'
import Icon from '../components/Icon.jsx'

export default function ProfilePage({ request, profile, user, onProfileUpdated, notify }) {
  const [values, setValues] = useState({ displayName: '', email: '', phone: '' })
  const [busy, setBusy] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')
  const fileInput = useRef(null)

  useEffect(() => {
    setValues({
      displayName: profile?.displayName || user?.username || '',
      email: profile?.email || '',
      phone: profile?.phone || '',
    })
  }, [profile, user])

  function update(key, value) {
    setValues(current => ({ ...current, [key]: value }))
  }

  async function save(event) {
    event.preventDefault()
    setError('')
    setBusy(true)
    try {
      const result = await request('/api/profile', {
        method: 'PUT',
        body: JSON.stringify(values),
      })
      onProfileUpdated(result)
      notify('Đã lưu thông tin cá nhân.')
    } catch (err) {
      setError(err.message || 'Không lưu được thông tin cá nhân.')
    } finally {
      setBusy(false)
    }
  }

  async function uploadAvatar(event) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    setError('')
    if (file.size > 5 * 1024 * 1024) {
      setError('Ảnh đại diện không được lớn hơn 5 MB.')
      return
    }
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      setError('Chọn ảnh JPG, PNG hoặc WEBP.')
      return
    }
    const form = new FormData()
    form.append('file', file)
    setUploading(true)
    try {
      const result = await request('/api/profile/avatar', { method: 'POST', body: form })
      onProfileUpdated(result)
      notify('Đã cập nhật ảnh đại diện lên MinIO.')
    } catch (err) {
      setError(err.message || 'Không tải được ảnh lên MinIO.')
    } finally {
      setUploading(false)
    }
  }

  const initials = (profile?.displayName || user?.username || 'U').trim().slice(0, 1).toUpperCase()

  return <div className="page-stack">
    <div className="page-intro"><div className="page-intro-icon"><Icon name="employee" size={21}/></div><div><h2>Thông tin cá nhân</h2><p>Cập nhật hồ sơ và ảnh đại diện của tài khoản.</p></div></div>
    <section className="content-card profile-editor-card">
      <div className="profile-editor-heading"><div className="profile-editor-avatar">{profile?.avatarUrl?<img src={profile.avatarUrl} alt="Ảnh đại diện"/>:<span>{initials}</span>}</div><div className="profile-editor-identity"><h2>{profile?.displayName || user?.username}</h2><p>@{user?.username} <span>·</span> {user?.role || 'USER'}</p><button type="button" className="button button-secondary profile-upload-button" onClick={()=>fileInput.current?.click()} disabled={uploading}>{uploading?<span className="spinner small-spinner"/>:<Icon name="employee" size={15}/>} {uploading?'Đang tải ảnh…':'Đổi ảnh đại diện'}</button><input ref={fileInput} className="hidden" type="file" accept="image/png,image/jpeg,image/webp" onChange={uploadAvatar}/><small>JPG, PNG hoặc WEBP · Tối đa 5 MB · Lưu trên MinIO</small></div></div>
      <div className="profile-divider"/>
      <form className="profile-form" onSubmit={save}>
        <div className="profile-form-grid">
          <label className="form-field"><span>Tên hiển thị</span><input autoComplete="name" value={values.displayName} onChange={event=>update('displayName',event.target.value)} maxLength={120} placeholder="Nhập họ tên"/></label>
          <label className="form-field"><span>Tên đăng nhập</span><input value={user?.username || ''} disabled readOnly/></label>
          <label className="form-field"><span>Email</span><input type="email" autoComplete="email" value={values.email} onChange={event=>update('email',event.target.value)} maxLength={190} placeholder="name@example.com"/></label>
          <label className="form-field"><span>Số điện thoại</span><input type="tel" autoComplete="tel" value={values.phone} onChange={event=>update('phone',event.target.value)} maxLength={30} placeholder="Nhập số điện thoại"/></label>
        </div>
        {error&&<div className="alert alert-error profile-error"><Icon name="incident" size={16}/>{error}</div>}
        <div className="profile-form-footer"><span>Thông tin hồ sơ chỉ hiển thị trong tài khoản của bạn.</span><button className="button button-primary" disabled={busy}>{busy?<span className="spinner small-spinner"/>:null}Lưu thay đổi</button></div>
      </form>
    </section>
  </div>
}
