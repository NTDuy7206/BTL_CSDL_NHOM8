import { rentalPeriod } from '../data/roomGroups.js'

function dateText(date) {
  return date ? date.slice(0, 10).split('-').reverse().join('/') : '—'
}

export default function RoomRentalPeriod({ contracts = [], unavailable = false }) {
  if (unavailable) return <span>Không tải được thông tin thuê</span>
  if (!contracts.length) return <span>Chưa xác định khách thuê</span>

  return <>
    {contracts.length > 1 && <span>Có nhiều hợp đồng đang hiệu lực</span>}
    {contracts.map(contract => {
      const { start, end, duration, unit } = rentalPeriod(contract)
      const tenantName = contract.tenant?.fullName?.trim()
      return <span className="room-rental-period" key={contract.contractId}>
        Khách thuê <strong>{tenantName || contract.tenant?.tenantId || 'Chưa xác định'}</strong><br/>
        Ngày đến {dateText(start)} · Ngày đi {dateText(end)}{duration != null && <> · <strong>{duration} {unit}</strong></>}
      </span>
    })}
  </>
}
