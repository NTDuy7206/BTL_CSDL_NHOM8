import { useCallback, useEffect, useState } from 'react'
import KeywordSearch from '../components/KeywordSearch.jsx'
import{matchesKeyword}from '../data/search.js'
import{requireRows}from '../data/listResponse.js'
import AdminUtilities from '../components/AdminUtilities.jsx'
import {todayVN} from '../data/finance.js'
import Icon from '../components/Icon.jsx'

function money(value) { return `${Number(value || 0).toLocaleString('vi-VN')} ₫` }
function date(value) { return value ? new Intl.DateTimeFormat('vi-VN',{dateStyle:'short',timeStyle:'short'}).format(new Date(value)) : '—' }
function methodLabel(value){return value==='CASH'?'Tiền mặt':['BANK_TRANSFER','BANK_TRANSFER_REPORTED'].includes(value)?'Chuyển khoản':value||'—'}
function statusLabel(value) { return value === 'PAID' ? 'Đã xác nhận' : value === 'REJECTED' ? 'Cần kiểm tra lại' : 'Chờ chủ nhà xác nhận' }

export function PaymentReviewCard({row,busy,receivedOn,onReview}){return <article className="payment-review-card"><div className="card-heading"><h3>{row.username} · {row.invoiceId}</h3><span className="status-pill status-pending">Chờ xác nhận</span></div><strong>{money(row.amount)}</strong><p>{methodLabel(row.paymentMethod)} · {row.transactionReference}</p><p>Gửi lúc: {date(row.createdAt)}</p><div className="workflow-buttons"><button className="button button-secondary" disabled={busy} onClick={()=>onReview(row,'REJECTED')}>Yêu cầu kiểm tra lại</button><button className="button button-primary" disabled={busy||!receivedOn} onClick={()=>onReview(row,'PAID')}>Xác nhận đã nhận tiền</button></div></article>}

export default function PaymentsPage({ request, role, notify,initialInvoiceId='' }) {
  const [query,setQuery]=useState('')
  const [view,setView]=useState('pending')
  const [receivedOn,setReceivedOn]=useState(todayVN())
  const [financeVersion,setFinanceVersion]=useState(0)
  const isTenant = role === 'USER'
  const [invoices, setInvoices] = useState([])
  const [reports, setReports] = useState([])
  const [invoiceId, setInvoiceId] = useState(initialInvoiceId)
  const [paymentMethod,setPaymentMethod]=useState('BANK_TRANSFER')
  const [reference, setReference] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setError('')
    try {
      const [reportRows, invoiceRows] = await Promise.all([
        request('/api/payment-reports'),
        isTenant ? request('/api/invoices') : Promise.resolve([]),
      ])
      setReports(requireRows(reportRows))
      setFinanceVersion(v=>v+1)
      setInvoices(requireRows(invoiceRows))
      setInvoiceId(current=>Array.isArray(invoiceRows)&&invoiceRows.some(i=>i.invoiceId===current)&&!reportRows.some(r=>r.invoiceId===current&&['PENDING','PAID'].includes(r.status))?current:'')
    } catch (err) { setError(err.message || 'Không tải được dữ liệu thanh toán.') }
  }, [request, isTenant])

  useEffect(() => { load();const timer=setInterval(load,30000);return()=>clearInterval(timer) }, [load])

  const pendingCount=reports.filter(row=>row.status==='PENDING').length
  const shownReports=reports.filter(row=>isTenant||(view==='pending'?row.status==='PENDING':row.status!=='PENDING')).filter(row=>matchesKeyword(row,query))
  const settledInvoiceIds = new Set(reports.filter(row => row.status === 'PENDING' || row.status === 'PAID').map(row => row.invoiceId))
  const payableInvoices = invoices.filter(invoice => !settledInvoiceIds.has(invoice.invoiceId))
  const selectedInvoice = invoices.find(invoice => invoice.invoiceId === invoiceId)

  async function submit(event) {
    event.preventDefault(); setBusy(true); setError('')
    try {
      await request('/api/payment-reports', { method: 'POST', body: JSON.stringify({ invoiceId, paymentMethod,transactionReference: reference }) })
      setInvoiceId('');setReference(''); notify('Đã gửi thông tin báo thanh toán để chủ nhà đối soát.')
      await load()
    } catch (err) { setError(err.message || 'Không gửi được báo cáo thanh toán.') }
    finally { setBusy(false) }
  }

  async function review(report, status) {
    setBusy(true); setError('')
    try {
      await request(`/api/payment-reports/${report.paymentReportId}/status`, { method: 'PATCH', body: JSON.stringify({ status,receivedOn }) })
      notify(status === 'PAID' ? 'Đã xác nhận thanh toán.' : 'Đã yêu cầu kiểm tra lại thông tin thanh toán.')
      await load()
    } catch (err) { setError(err.message || 'Không cập nhật được trạng thái.') }
    finally { setBusy(false) }
  }

  return <div className="page-stack">
    <div className="page-intro"><div className="page-intro-icon"><Icon name="invoice" size={21}/></div><div><h2>Thanh toán</h2><p>{isTenant?'Chọn hóa đơn, phương thức thanh toán và gửi thông tin để chủ nhà xác nhận.':'Xác nhận tiền khách thuê ở đầu trang; phần chi trả điện/nước nhà cung cấp nằm bên dưới.'}</p></div><span className="intro-spacer"/><button className="icon-button refresh-button" onClick={load} title="Tải lại"><Icon name="refresh"/></button></div>
    {isTenant&&<section className="content-card workflow-card"><div className="workflow-heading"><span className="workflow-icon"><Icon name="invoice"/></span><div><h2>Báo đã thanh toán hóa đơn</h2><p>Chọn chuyển khoản hoặc tiền mặt. Chủ nhà sẽ kiểm tra và xác nhận đã nhận tiền.</p></div></div>
      <form className="workflow-form" onSubmit={submit}><label className="form-field"><span>Hóa đơn cần thanh toán</span><select required value={invoiceId} onChange={event=>setInvoiceId(event.target.value)}><option value="">Chọn hóa đơn</option>{payableInvoices.map(invoice=><option key={invoice.invoiceId} value={invoice.invoiceId}>{invoice.invoiceId} · Tháng {invoice.month}/{invoice.year} · {money(invoice.totalAmount)}</option>)}</select></label>{selectedInvoice&&<div className="payment-amount"><span>Số tiền hóa đơn</span><b>{money(selectedInvoice.totalAmount)}</b></div>}<label className="form-field"><span>Phương thức thanh toán</span><select required value={paymentMethod} onChange={e=>{setPaymentMethod(e.target.value);setReference('')}}><option value="BANK_TRANSFER">Chuyển khoản ngân hàng</option><option value="CASH">Tiền mặt trực tiếp cho chủ nhà</option></select></label><label className="form-field workflow-message"><span>{paymentMethod==='CASH'?'Thông tin giao tiền / mã biên nhận':'Mã giao dịch / nội dung chuyển khoản'}</span><input required maxLength="100" value={reference} onChange={event=>setReference(event.target.value)} placeholder={paymentMethod==='CASH'?'Ví dụ: Đã giao tiền ngày 29/10, biên nhận 01':'Nhập mã giao dịch hoặc nội dung chuyển khoản'}/><small>{paymentMethod==='CASH'?'Chỉ gửi báo cáo sau khi đã giao tiền; chủ nhà sẽ xác nhận.':'Chuyển tiền theo tài khoản do chủ nhà cung cấp rồi gửi mã giao dịch tại đây.'}</small></label><button className="button button-primary" disabled={busy || !invoiceId}><Icon name="check" size={16}/>{busy?'Đang gửi…':'Gửi thông tin thanh toán'}</button>{payableInvoices.length===0&&<small className="workflow-help">Không có hóa đơn mới để báo. Hóa đơn đang chờ xác nhận sẽ xuất hiện trong danh sách bên dưới.</small>}</form><p className="payment-disclaimer">Đây là bước ghi nhận và đối soát thủ công; ứng dụng hiện chưa kết nối ngân hàng hoặc cổng thanh toán trực tuyến.</p>
    </section>}
    {error&&<div className="alert alert-error"><Icon name="incident" size={16}/>{error}</div>}
    <section className="content-card workflow-card"><div className="card-heading"><div><h2>{isTenant?'Lịch sử báo thanh toán':'Xác nhận thanh toán của khách thuê'}</h2><p>{reports.length} báo cáo thanh toán</p></div><span className="module-count">{reports.filter(row=>row.status==='PENDING').length} đang chờ</span></div>
      {!isTenant&&<><div className="workflow-buttons"><button className={`button ${view==='pending'?'button-primary':'button-secondary'}`} onClick={()=>setView('pending')}>Chờ xác nhận ({pendingCount})</button><button className={`button ${view==='history'?'button-primary':'button-secondary'}`} onClick={()=>setView('history')}>Đã xử lý ({reports.length-pendingCount})</button></div><p>Chỉ hiển thị báo thanh toán của khách thuê phòng thuộc chủ nhà đang đăng nhập. Danh sách tự cập nhật mỗi 30 giây.</p></>}
      {!isTenant&&<label className="form-field"><span>Ngày thực nhận tiền khi xác nhận đã thu</span><input type="date" required min="2000-01-01" max={todayVN()} value={receivedOn} onChange={e=>setReceivedOn(e.target.value)}/></label>}
      <KeywordSearch value={query} onChange={setQuery} label="Tìm báo cáo thanh toán theo từ khóa"/><p>{shownReports.length} / {reports.length} báo cáo</p>
      {!isTenant&&view==='pending'?<div className="payment-review-list">{shownReports.map(row=><PaymentReviewCard key={row.paymentReportId} row={row} busy={busy} receivedOn={receivedOn} onReview={review}/>)}{!shownReports.length&&<p>{query?'Không có báo cáo khớp từ khóa.':'Chưa có khoản chờ xác nhận cho chủ nhà này. Có thể xem các khoản đã xử lý ở tab bên cạnh.'}</p>}</div>:shownReports.length ? <div className="payment-table-wrap"><table className="payment-table"><thead><tr><th>Hóa đơn</th>{!isTenant&&<th>Tài khoản</th>}<th>Số tiền</th><th>Phương thức</th><th>Thông tin thanh toán</th><th>Gửi lúc</th><th>Trạng thái</th>{!isTenant&&<th>Đối soát</th>}</tr></thead><tbody>{shownReports.map(row=><tr key={row.paymentReportId}><td><b>{row.invoiceId}</b></td>{!isTenant&&<td>{row.username}</td>}<td>{money(row.amount)}</td><td>{methodLabel(row.paymentMethod)}</td><td>{row.transactionReference}</td><td>{date(row.createdAt)}</td><td><span className={`status-pill status-${String(row.status).toLowerCase()}`}>{statusLabel(row.status)}</span></td>{!isTenant&&<td>{row.status==='PENDING'&&<div className="workflow-buttons"><button className="button button-secondary" disabled={busy} onClick={()=>review(row,'REJECTED')}>Kiểm tra lại</button><button className="button button-primary" disabled={busy||!receivedOn} onClick={()=>review(row,'PAID')}>Xác nhận đã thu</button></div>}</td>}</tr>)}</tbody></table></div> : <div className="workflow-empty"><Icon name="invoice" size={22}/><span>{query?'Không tìm thấy báo cáo khớp từ khóa.':'Chưa có báo cáo thanh toán.'}</span></div>}
    </section>
    {!isTenant&&<AdminUtilities request={request} notify={notify} refreshVersion={financeVersion}/>}
  </div>
}
