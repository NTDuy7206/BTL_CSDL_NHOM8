import { rentalPeriod } from '../data/roomGroups.js'

function dateText(date) {
  return date ? date.slice(0, 10).split('-').reverse().join('/') : '—'
}

export default function RoomRentalPeriod({ contracts = [], unavailable = false }) {
  if (unavailable) return <span>Không tải được thời gian thuê</span>
  if (!contracts.length) return <span>Chưa xác định thời gian thuê</span>
  if (contracts.length > 1) return <span>Có nhiều hợp đồng đang hiệu lực</span>

  const { start, end, duration, unit } = rentalPeriod(contracts[0])
  return <span className="room-rental-period">
    Ngày đến {dateText(start)} · Ngày đi {dateText(end)}{duration != null && <> · <strong>{duration} {unit}</strong></>}
  </span>
}
