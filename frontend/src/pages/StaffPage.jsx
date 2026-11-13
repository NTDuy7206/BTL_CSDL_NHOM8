import {useCallback,useEffect,useState} from 'react'
import {money} from '../data/finance.js'
import {isComplete} from '../data/completion.js'

const titles = {cleaning:'Dọn dẹp',incidents:'Xử lý sự cố'}
const endpoint = '/api/staff'

function dateLabel(date) {
  return date ? date.split('-').reverse().join('/') : '—'
}

function Payroll({rows,admin,submit,busy}) {
  return <section className="content-card workflow-card"><h2>{admin?'Bảng lương nhân viên':'Lương của tôi'}</h2>
    {rows.length ? <div className="payment-table-wrap"><table className="payment-table">
      <thead><tr><th>Kỳ lương</th>{admin&&<th>Nhân viên</th>}<th>Lương cơ bản</th><th>Thâm niên</th><th>Hệ số</th><th>Tiền lương</th><th>Trạng thái</th></tr></thead>
      <tbody>{rows.map(row=><tr key={`${row.employeeId}-${row.period}`}>
        <td>{row.period.slice(0,7)}</td>{admin&&<td>{row.fullName || row.employeeId}</td>}<td>{money(row.baseSalary)}</td>
        <td>{row.seniority} năm</td><td>{Number(row.multiplier).toLocaleString('vi-VN')}</td><td>{money(row.amount)}</td>
        <td>{row.paidOn ? `Đã trả ${row.paidOn}` : admin ? <button className="button button-primary" disabled={busy} onClick={()=>{
          if (window.confirm(`Xác nhận đã trả ${money(row.amount)} cho ${row.fullName || row.employeeId}?`)) {
            submit(`${endpoint}/payroll/${encodeURIComponent(row.employeeId)}/${row.period}/paid`,undefined,'Đã ghi nhận trả lương.')
          }
        }}>Ghi nhận đã trả</button> : 'Chưa thanh toán'}</td>
      </tr>)}</tbody>
    </table></div> : <p>Chưa có kỳ lương đến hạn.</p>}
  </section>
}

function WorkList({kind,rows,submit,busy}) {
  const [results,setResults]=useState({})
  const [showCompleted,setShowCompleted]=useState(false)
  const completed=row=>kind==='incidents'?isComplete('incidents',row):Boolean(row.completed)
  const pending=rows.filter(row=>!completed(row))
  const visible=showCompleted?rows.filter(completed):pending
  return <section className="content-card workflow-card"><h2>{titles[kind]}</h2>
    <div className="workflow-buttons">
      <button className={`button ${showCompleted?'button-secondary':'button-primary'}`} onClick={()=>setShowCompleted(false)}>Chờ xử lý ({pending.length})</button>
      <button className={`button ${showCompleted?'button-primary':'button-secondary'}`} onClick={()=>setShowCompleted(true)}>Đã xong ({rows.length-pending.length})</button>
    </div>
    <div className="workflow-list">{visible.map(row=><article className="workflow-row" key={row.id}>
      <div className="workflow-main"><b>Phòng {row.roomId} · {row.id}</b><p>{row.content}</p>{row.result&&<p>{row.result}</p>}
        {!showCompleted&&kind==='incidents'&&<label className="form-field"><span>Kết quả xử lý</span>
          <textarea rows={2} maxLength={500} value={results[row.id]||''} onChange={e=>setResults(current=>({...current,[row.id]:e.target.value}))}/>
        </label>}
      </div>
      {showCompleted ? <span className="status-pill status-approved">Đã hoàn thành</span> :
        <button className="button button-primary" disabled={busy||(kind==='incidents'&&!results[row.id]?.trim())}
          onClick={()=>submit(`${endpoint}/me/${kind==='incidents'?'reported-incidents':kind}/${encodeURIComponent(row.id)}/complete`,kind==='incidents'?{result:results[row.id]}:undefined,'Đã hoàn thành công việc.')}>Hoàn thành</button>}
    </article>)}</div>
  </section>
}

function StaffSalaries({rows}) {
  return <section className="content-card workflow-card"><h2>Lương dự kiến của nhân viên</h2>
    {rows.length ? <div className="payment-table-wrap"><table className="payment-table">
      <thead><tr><th>Nhân viên</th><th>Công việc</th><th>Ngày vào làm</th><th>Lương cơ bản</th><th>Thâm niên</th><th>Lương dự kiến</th></tr></thead>
      <tbody>{rows.map(row=><tr key={row.employeeId}><td>{row.fullName || row.employeeId}</td><td>{row.position || '—'}</td>
        <td>{dateLabel(row.hiredOn)}</td>
        <td>{row.baseSalary>0?money(row.baseSalary):'—'}</td><td>{row.seniority==null?'—':`${row.seniority} năm`}</td>
        <td>{row.baseSalary>0&&row.amount!=null?money(row.amount):'Chưa cấu hình'}</td></tr>)}</tbody>
    </table></div> : <p>Chưa có nhân viên.</p>}
  </section>
}

export default function StaffPage({mode,request,notify,onNavigate}) {
  const [data,setData]=useState(null)
  const [busy,setBusy]=useState(false), [error,setError]=useState('')
  const load=useCallback(async()=>{
    try{setData(await request(endpoint+(mode==='admin-payroll'?'/payroll':'/me')))}
    catch(err){setError(err.message)}
  },[mode,request])
  useEffect(()=>{load()},[load])
  useEffect(()=>{
    if(mode!=='work')return
    const timer=window.setInterval(load,15000)
    return()=>window.clearInterval(timer)
  },[load,mode])

  async function submit(path,payload,success){
    if(busy)return
    setBusy(true);setError('')
    try{
      await request(path,{method:'PUT',...(payload?{body:JSON.stringify(payload)}:{})})
      notify(success);await load()
    }catch(err){setError(err.message)}finally{setBusy(false)}
  }

  return <div className="page-stack">
    {error&&<div className="alert alert-error" role="alert">{error}</div>}
    {data==null&&!error&&<p>Đang tải…</p>}
    {mode==='admin-payroll'&&data&&<><StaffSalaries rows={data.employees}/><Payroll rows={data.history} admin submit={submit} busy={busy}/></>}
    {data?.linked===false&&<section className="content-card workflow-card"><h2>Chưa liên kết với chủ nhà</h2><button className="button button-primary" onClick={()=>onNavigate('staff-applications')}>Xin việc</button></section>}
    {data?.linked&&mode==='employee-payroll'&&<>
      <section className="content-card workflow-card"><h2>Lương dự kiến</h2>
        <p>Ngày vào làm <strong>{dateLabel(data.employee.hiredOn)}</strong> · Ngày trả lương kỳ này <strong>{dateLabel(data.payday)}</strong></p>
        {data.employee.baseSalary>0&&data.employee.amount!=null?<p>{money(data.employee.baseSalary)} × {Number(data.employee.multiplier).toLocaleString('vi-VN')} ({data.employee.seniority} năm thâm niên) = <strong>{money(data.employee.amount)}</strong></p>:<p>Chưa có đủ thông tin để tính lương.</p>}
      </section><Payroll rows={data.payroll} />
    </>}
    {data?.linked&&mode==='work'&&<><WorkList kind="cleaning" rows={data.cleaning} submit={submit} busy={busy}/><WorkList kind="incidents" rows={data.incidents} submit={submit} busy={busy}/></>}
  </div>
}
