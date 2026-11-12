export default function DepositPayment({deposit, admin=false, onRefresh}) {
  if (!deposit?.requestId) return null

  const waiting = deposit.status === 'AWAITING_PAYMENT'
  const canTransfer = waiting && deposit.bankReady && !deposit.reportedAt
  const statuses = {
    PAID: 'Đã nhận cọc',
    REVIEW: 'Chờ chủ nhà chấp thuận yêu cầu cọc',
    CANCELLED: 'Đã hủy hóa đơn cọc',
  }
  let status = statuses[deposit.status]
  if (waiting) {
    status = deposit.reportedAt ? 'Khách đã báo chuyển khoản, chờ chủ nhà xác nhận' : 'Chờ chuyển khoản'
    if (!deposit.bankReady) status = 'Chủ nhà cần cập nhật thông tin ngân hàng'
  }

  return <section className="deposit-payment">
    <h3>Đặt cọc {Number(deposit.amount).toLocaleString('vi-VN')} đ</h3>
    <p>{status}</p>
    {deposit.bankReference && <p>Mã giao dịch ngân hàng <strong>{deposit.bankReference}</strong></p>}
    {canTransfer && <>
      <dl>
        <dt>Ngân hàng</dt><dd>{deposit.bankName}</dd>
        <dt>Tài khoản nhận</dt><dd>{deposit.accountNumber}</dd>
        <dt>Chủ tài khoản</dt><dd>{deposit.accountHolder}</dd>
        <dt>Nội dung chuyển</dt><dd><strong>{deposit.paymentCode}</strong></dd>
      </dl>
      {!admin && deposit.qrUrl && <img src={deposit.qrUrl} referrerPolicy="no-referrer" alt="QR chuyển khoản cọc" className="deposit-qr"/>}
      {!admin && <p>Kiểm tra người nhận trong ứng dụng ngân hàng và giữ nguyên số tiền, nội dung chuyển khoản.</p>}
    </>}
    {onRefresh && <button type="button" className="button button-secondary" onClick={onRefresh}>Cập nhật trạng thái cọc</button>}
  </section>
}
