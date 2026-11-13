import {useCallback,useEffect,useState} from 'react'

const statuses = {PENDING:'Chờ duyệt',APPROVED:'Đã chấp nhận',REJECTED:'Đã từ chối',CLOSED:'Đã đóng vì đã liên kết với chủ nhà'}

export default function StaffApplicationsPage({admin,request,notify,onNavigate}) {
  const endpoint = admin ? '/api/staff/applications' : '/api/staff/me/applications'
  const [data,setData] = useState(null)
  const [landlord,setLandlord] = useState(''), [message,setMessage] = useState('')
  const [profiles,setProfiles] = useState({}), [busy,setBusy] = useState(false), [error,setError] = useState('')
  const load = useCallback(async()=>{
    try { setData(await request(endpoint)) } catch(err) { setError(err.message) }
  },[endpoint,request])
  useEffect(()=>{load()},[load])

  async function submit(path,method,payload,success) {
    if (busy) return
    setBusy(true)
    setError('')
    try {
      await request(path,{method,body:JSON.stringify(payload)})
      notify(success)
      if (!admin) { setLandlord(''); setMessage('') }
      await load()
    } catch(err) { setError(err.message) } finally { setBusy(false) }
  }

  function decide(id,status) {
    const success = status==='APPROVED'?'Đã duyệt đơn và liên kết tài khoản.':'Đã từ chối đơn.'
    return submit(`${endpoint}/${id}`,'PUT',{status,employeeId:profiles[id]},success)
  }

  const availableProfiles = (data?.profiles ?? []).filter(profile=>!profile.username)
  return <div className="page-stack">
    {error&&<div className="alert alert-error" role="alert">{error}</div>}
    {data&&!admin&&!data.linked&&<section className="content-card workflow-card">
      <h2>Gửi đơn xin việc</h2>
      <form className="workflow-form" onSubmit={event=>{event.preventDefault();submit(endpoint,'POST',{landlordId:Number(landlord),message},'Đã gửi đơn xin việc.')}}>
        <label className="form-field"><span>Chủ nhà</span><select value={landlord} onChange={event=>setLandlord(event.target.value)} required>
          <option value="">Chọn chủ nhà</option>
          {data.landlords.map(owner=><option key={owner.id} value={owner.id}>{owner.displayName===owner.username?owner.username:`${owner.displayName} (${owner.username})`}</option>)}
        </select></label>
        <label className="form-field"><span>Lời nhắn</span><textarea rows={3} value={message} maxLength={500} onChange={event=>setMessage(event.target.value)}/></label>
        <button className="button button-primary" disabled={busy||!landlord}>Gửi đơn</button>
      </form>
    </section>}
    <section className="content-card workflow-card">
      <div className="card-heading"><h2>{admin?'Đơn xin việc':'Đơn đã gửi'}</h2><button className="button button-secondary" onClick={load} disabled={busy}>Tải lại</button></div>
      {!data&&!error&&<p>Đang tải…</p>}
      {data?.applications.length===0&&<p>Chưa có đơn xin việc.</p>}
      {data?.linked&&<p>Bạn đã liên kết với chủ nhà. <button className="button button-secondary" onClick={()=>onNavigate('dashboard')}>Xem công việc</button></p>}
      {admin&&data?.applications.some(row=>row.status==='PENDING')&&!availableProfiles.length&&<p>Tạo một hồ sơ nhân viên trong mục <button className="button button-secondary" onClick={()=>onNavigate('employees')}>Nhân viên</button> để duyệt đơn.</p>}
      <div className="workflow-list">{data?.applications.map(row=><article className="workflow-row" key={row.id}>
        <div className="workflow-main"><b>{row.displayName}</b>{row.displayName!==row.username&&<span>@{row.username}</span>}<span>{row.submittedOn}</span>{row.message&&<p>{row.message}</p>}</div>
        <span className={`status-pill status-${row.status.toLowerCase()}`}>{statuses[row.status]}</span>
        {admin&&row.status==='PENDING'&&<div className="workflow-buttons">
          <label className="form-field"><span>Hồ sơ nhân viên</span><select value={profiles[row.id]||''} onChange={event=>setProfiles(current=>({...current,[row.id]:event.target.value}))}>
            <option value="">Chọn hồ sơ</option>{availableProfiles.map(profile=><option key={profile.employeeId} value={profile.employeeId}>{profile.employeeId} · {profile.fullName}</option>)}
          </select></label>
          <button className="button button-primary" disabled={busy||!profiles[row.id]} onClick={()=>decide(row.id,'APPROVED')}>Chấp nhận</button>
          <button className="button button-secondary" disabled={busy} onClick={()=>decide(row.id,'REJECTED')}>Từ chối</button>
        </div>}
      </article>)}</div>
    </section>
  </div>
}
