import {useCallback,useEffect,useState} from 'react'
import {money} from '../data/finance.js'

const statuses = {PENDING:'Chờ duyệt',APPROVED:'Đã chấp nhận',REJECTED:'Đã từ chối',CLOSED:'Đã đóng vì đã liên kết với chủ nhà'}

function submissionTime(row) {
  const [date,time] = (row.submittedAt || row.submittedOn).split('T')
  const [year,month,day] = date.split('-')
  const localDate = `${day}/${month}/${year}`
  return row.submittedAt ? `${localDate} · ${time.slice(0,5)}` : localDate
}

export default function StaffApplicationsPage({admin,request,notify,onNavigate}) {
  const endpoint = admin ? '/api/staff/applications' : '/api/staff/me/applications'
  const [data,setData] = useState(null)
  const [landlord,setLandlord] = useState(''), [message,setMessage] = useState('')
  const [jobCode,setJobCode] = useState(''), [legacyJobs,setLegacyJobs] = useState({})
  const [busy,setBusy] = useState(false), [error,setError] = useState('')
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
      if (!admin) { setLandlord(''); setMessage(''); setJobCode('') }
      await load()
    } catch(err) { setError(err.message) } finally { setBusy(false) }
  }

  function decide(id,status) {
    const success = status==='APPROVED'?'Đã duyệt đơn và tạo hồ sơ nhân viên.':'Đã từ chối đơn.'
    return submit(`${endpoint}/${id}`,'PUT',{status,jobCode:legacyJobs[id]},success)
  }

  const jobs = data?.jobs ?? []
  const jobOptions = jobs.map(job=><option key={job.code} value={job.code}>{job.label} · {money(job.baseSalary)}/tháng</option>)
  return <div className="page-stack">
    {error&&<div className="alert alert-error" role="alert">{error}</div>}
    {data&&!admin&&!data.linked&&<section className="content-card workflow-card">
      <h2>Gửi đơn xin việc</h2>
      <form className="workflow-form" onSubmit={event=>{event.preventDefault();submit(endpoint,'POST',{landlordId:Number(landlord),message,jobCode},'Đã gửi đơn xin việc.')}}>
        <label className="form-field"><span>Chủ nhà</span><select value={landlord} onChange={event=>setLandlord(event.target.value)} required>
          <option value="">Chọn chủ nhà</option>
          {data.landlords.map(owner=><option key={owner.id} value={owner.id}>{owner.displayName===owner.username?owner.username:`${owner.displayName} (${owner.username})`}</option>)}
        </select></label>
        <label className="form-field"><span>Công việc và lương cơ bản</span><select required value={jobCode} onChange={event=>setJobCode(event.target.value)}>
          <option value="">Chọn công việc</option>{jobOptions}
        </select></label>
        <label className="form-field"><span>Lời nhắn</span><textarea rows={3} value={message} maxLength={500} onChange={event=>setMessage(event.target.value)}/></label>
        <button className="button button-primary" disabled={busy||!landlord||!jobCode}>Gửi đơn</button>
      </form>
    </section>}
    <section className="content-card workflow-card">
      <div className="card-heading"><h2>{admin?'Đơn xin việc':'Đơn đã gửi'}</h2><button className="button button-secondary" onClick={load} disabled={busy}>Tải lại</button></div>
      {!data&&!error&&<p>Đang tải…</p>}
      {data?.linked&&<p><button className="button button-secondary" onClick={()=>onNavigate('dashboard')}>Xem công việc</button></p>}
      <div className="workflow-list">{data?.applications.map(row=>{const job=jobs.find(item=>item.code===row.jobCode);return <article className="workflow-row" key={row.id}>
        <div className="workflow-main"><b>{row.displayName}</b>{row.displayName!==row.username&&<span>@{row.username}</span>}<time dateTime={row.submittedAt || row.submittedOn}>{submissionTime(row)}</time>{job&&<span>{job.label} · {money(job.baseSalary)}/tháng</span>}{row.message&&<p>{row.message}</p>}</div>
        <span className={`status-pill status-${row.status.toLowerCase()}`}>{statuses[row.status]}</span>
        {admin&&row.status==='PENDING'&&<div className="workflow-buttons">
          {!job&&<label className="form-field"><span>Công việc cho đơn cũ</span><select value={legacyJobs[row.id]||''} onChange={event=>setLegacyJobs(current=>({...current,[row.id]:event.target.value}))}>
            <option value="">Chọn công việc</option>{jobOptions}
          </select></label>}
          <button className="button button-primary" disabled={busy||(!job&&!legacyJobs[row.id])} onClick={()=>decide(row.id,'APPROVED')}>Chấp nhận</button>
          <button className="button button-secondary" disabled={busy} onClick={()=>decide(row.id,'REJECTED')}>Từ chối</button>
        </div>}
      </article>})}</div>
    </section>
  </div>
}
