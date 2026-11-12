import {useState} from 'react'
import DepositPayment from './DepositPayment.jsx'

function DepositInvoice({invoice, admin, request, onChange}) {
  const [reference, setReference] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const waiting = invoice.status === 'AWAITING_PAYMENT'

  async function submit(event) {
    event.preventDefault()
    if (busy) return
    if (admin && !window.confirm('Bạn đã kiểm tra tiền cọc thực sự vào tài khoản và đúng số tiền của hóa đơn này?')) return
    setBusy(true)
    setError('')
    try {
      const action = admin ? 'confirm' : 'report'
      await request(`/api/deposit-invoices/${invoice.requestId}/${action}`, {
        method: 'POST',
        ...(admin ? {} : {body: JSON.stringify({bankReference: reference.trim()})}),
      })
      await onChange()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return <article className="content-card workflow-card">
    <h3>Hóa đơn cọc {invoice.paymentCode}</h3>
    <p>Phòng {invoice.roomId} · Yêu cầu #{invoice.requestId}</p>
    <DepositPayment deposit={invoice} admin={admin}/>
    {waiting && !admin && invoice.bankReady && !invoice.reportedAt && <form className="rental-form" onSubmit={submit}>
      <label className="form-field">
        <span>Mã giao dịch trên biên lai ngân hàng</span>
        <input required maxLength={100} value={reference} onChange={e => setReference(e.target.value)}/>
      </label>
      <button className="button button-primary" disabled={busy || !reference.trim()}>Tôi đã chuyển khoản</button>
    </form>}
    {waiting && admin && invoice.reportedAt && <form onSubmit={submit}>
      <button className="button button-primary" disabled={busy}>Đã nhận cọc</button>
    </form>}
    {error && <p className="alert alert-error" role="alert">{error}</p>}
  </article>
}

export default function DepositInvoices({invoices, admin, request, onChange}) {
  if (!invoices.length) return null
  return <section className="page-stack" aria-label="Hóa đơn đặt cọc">
    <h2>Hóa đơn đặt cọc</h2>
    {invoices.map(invoice => <DepositInvoice key={invoice.requestId} invoice={invoice} admin={admin} request={request} onChange={onChange}/>)}
  </section>
}
