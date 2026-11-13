import {useCallback,useEffect,useState} from 'react'
import {money} from '../data/finance.js'

const titles = {cleaning:'Dọn dẹp',incidents:'Xử lý sự cố'}
const endpoint = '/api/staff'

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
    </table></div> : <p>Chưa có kỳ lương đến hạn. Kỳ lương được tạo vào ngày cuối tháng.</p>}
  </section>
}

function WorkList({kind,rows,submit,busy}) {
  const [results,setResults]=useState({})
  return <section className="content-card workflow-card"><h2>{titles[kind]}</h2>
    {!rows.length&&<p>Chưa có công việc được giao.</p>}
    <div className="workflow-list">{rows.map(row=><article className="workflow-row" key={row.id}>
      <div className="workflow-main"><b>Phòng {row.roomId} · {row.id}</b><p>{row.content}</p>{row.result&&<p>{row.result}</p>}
        {!row.completed&&kind==='incidents'&&<label className="form-field"><span>Kết quả xử lý</span>
          <textarea rows={2} maxLength={500} value={results[row.id]||''} onChange={e=>setResults(current=>({...current,[row.id]:e.target.value}))}/>
        </label>}
      </div>
      {row.completed ? <span className="status-pill status-approved">Đã hoàn thành</span> :
        <button className="button button-primary" disabled={busy||(kind==='incidents'&&!results[row.id]?.trim())}
          onClick={()=>submit(`${endpoint}/me/${kind}/${encodeURIComponent(row.id)}/complete`,kind==='incidents'?{result:results[row.id]}:undefined,'Đã hoàn thành công việc.')}>Hoàn thành</button>}
    </article>)}</div>
  </section>
}

export default function StaffPage({mode,request,notify,onNavigate}) {
  const [data,setData]=useState(null), [employeeId,setEmployee]=useState(''), [username,setUsername]=useState('')
  const [busy,setBusy]=useState(false), [error,setError]=useState('')
  const load=useCallback(async()=>{
    try{setData(await request(endpoint+(mode==='directory'?'/directory':mode==='admin-payroll'?'/payroll':'/me')))}
    catch(err){setError(err.message)}
  },[mode,request])
  useEffect(()=>{load()},[load])

  async function submit(path,payload,success){
    if(busy)return
    setBusy(true);setError('')
    try{
      await request(path,{method:'PUT',...(payload?{body:JSON.stringify(payload)}:{})})
      notify(success);setUsername('');await load()
    }catch(err){setError(err.message)}finally{setBusy(false)}
  }

  return <div className="page-stack">
    {error&&<div className="alert alert-error" role="alert">{error}</div>}
    {data==null&&!error&&<p>Đang tải…</p>}
    {mode==='directory'&&data&&<section className="content-card workflow-card"><h2>Liên kết tài khoản nhân viên</h2>
      <p>Nhân viên đăng ký tài khoản trước. Chọn hồ sơ đã lập trong mục Nhân viên, rồi nhập tên đăng nhập của họ.</p>
      <form className="workflow-form" onSubmit={event=>{event.preventDefault();submit(`${endpoint}/directory/${encodeURIComponent(employeeId)}/account`,{username},'Đã liên kết tài khoản nhân viên.')}}>
        <label className="form-field"><span>Hồ sơ nhân viên</span><select required value={employeeId} onChange={e=>setEmployee(e.target.value)}>
          <option value="">Chọn hồ sơ</option>{data.filter(row=>!row.username).map(row=><option key={row.employeeId} value={row.employeeId}>{row.employeeId} · {row.fullName}</option>)}
        </select></label>
        <label className="form-field"><span>Tên đăng nhập</span><input required value={username} onChange={e=>setUsername(e.target.value)}/></label>
        <button className="button button-primary" disabled={busy||!employeeId}>Liên kết</button>
      </form>
      <div className="workflow-list">{data.filter(row=>row.username).map(row=><p key={row.employeeId}>{row.employeeId} · {row.fullName} · {row.username}</p>)}</div>
    </section>}
    {mode==='admin-payroll'&&data&&<Payroll rows={data} admin submit={submit} busy={busy}/>}
    {data?.linked===false&&<section className="content-card workflow-card"><h2>Chưa liên kết với chủ nhà</h2><button className="button button-primary" onClick={()=>onNavigate('staff-applications')}>Xin việc</button></section>}
    {data?.linked&&mode==='employee-payroll'&&<>
      <section className="content-card workflow-card"><h2>Lương dự kiến</h2>
        {data.employee.baseSalary>0&&data.employee.estimatedSalary!=null?<p>{money(data.employee.baseSalary)} × {Number(data.employee.multiplier).toLocaleString('vi-VN')} ({data.employee.seniority} năm thâm niên) = <strong>{money(data.employee.estimatedSalary)}</strong></p>:<p>Chủ nhà cần cập nhật lương cơ bản và thâm niên.</p>}
      </section><Payroll rows={data.payroll} />
    </>}
    {data?.linked&&mode==='work'&&<><WorkList kind="cleaning" rows={data.cleaning} submit={submit} busy={busy}/><WorkList kind="incidents" rows={data.incidents} submit={submit} busy={busy}/></>}
  </div>
}
