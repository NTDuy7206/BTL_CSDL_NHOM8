export const modules = [
  {
    key: 'tenants', title: 'Khách thuê', singular: 'khách thuê', icon: 'users', path: '/api/tenants', id: 'tenantId',
    columns: ['tenantId', 'fullName', 'cccd', 'phoneNumber', 'dateOfBirth'],
    fields: [
      ['tenantId', 'Mã khách thuê', 'text'], ['cccd', 'CCCD', 'text'], ['fullName', 'Họ và tên', 'text'],
      ['dateOfBirth', 'Ngày sinh', 'date'], ['permanentAddress', 'Địa chỉ thường trú', 'textarea'], ['phoneNumber', 'Số điện thoại', 'tel'],
    ],
  },
  {
    key: 'employees', title: 'Nhân viên', singular: 'nhân viên', icon: 'employee', path: '/api/employees', id: 'employeeId',
    columns: ['employeeId', 'fullName', 'position', 'phoneNumber', 'baseSalary'],
    fields: [
      ['employeeId', 'Mã nhân viên', 'text'], ['cccd', 'CCCD', 'text'], ['fullName', 'Họ và tên', 'text'],
      ['position', 'Vị trí công việc', 'text'], ['baseSalary', 'Lương cơ bản', 'number'], ['seniority', 'Thâm niên (năm)', 'number'],
      ['dateOfBirth', 'Ngày sinh', 'date'], ['phoneNumber', 'Số điện thoại', 'tel'],
    ],
  },
  {
    key: 'rooms', title: 'Phòng / mặt bằng', singular: 'phòng', icon: 'building', path: '/api/rooms', id: 'roomId',
    columns: ['roomId', 'floor', 'area', 'roomType', 'status', 'basePrice'],
    fields: [
      ['roomId', 'Mã phòng', 'text'], ['floor', 'Tầng', 'number'], ['area', 'Diện tích (m²)', 'number'],
      ['roomType', 'Loại phòng', 'text'], ['status', 'Tình trạng', 'text'], ['basePrice', 'Giá thuê cơ bản', 'number'],
    ],
  },
  {
    key: 'contracts', title: 'Hợp đồng', singular: 'hợp đồng', icon: 'contract', path: '/api/contracts', id: 'contractId',
    columns: ['contractId', 'tenantName', 'tenantBirth', 'tenantPhone', 'roomId', 'startDate', 'endDate', 'actualCheckIn', 'actualCheckOut', 'agreedRent', 'deposit'],
    fields: [
      ['contractId', 'Mã hợp đồng', 'text'], ['startDate', 'Ngày bắt đầu', 'date'], ['endDate', 'Ngày kết thúc', 'date'],
      ['deposit', 'Tiền cọc', 'number'], ['roomId', 'Mã phòng', 'text'], ['tenantId', 'Mã khách thuê', 'text'],
    ],
  },
  {
    key: 'invoices', title: 'Hóa đơn', singular: 'hóa đơn', icon: 'invoice', path: '/api/invoices', id: 'invoiceId',
    columns: ['invoiceId', 'month', 'year', 'rentAmount', 'electricityAmount', 'waterAmount', 'totalAmount', 'contractId'],
    fields: [
      ['invoiceId', 'Mã hóa đơn', 'text'], ['month', 'Tháng', 'number'], ['year', 'Năm', 'number'], ['rentAmount', 'Tiền thuê', 'number'],
      ['electricityAmount', 'Tiền điện', 'number'], ['waterAmount', 'Tiền nước', 'number'], ['fineAmount', 'Tiền phạt', 'number'],
      ['totalAmount', 'Tổng tiền', 'number'], ['contractId', 'Mã hợp đồng', 'text'],
    ],
  },
  {
    key: 'utility-readings', title: 'Chỉ số điện nước', singular: 'chỉ số điện nước', icon: 'meter', path: '/api/utility-readings', id: 'readingId',
    columns: ['readingId', 'month', 'year', 'roomId', 'electricityStartIndex', 'electricityEndIndex', 'electricityUsage', 'waterStartIndex', 'waterEndIndex', 'waterUsage'],
    fields: [
      ['readingId', 'Mã chỉ số', 'text'], ['month', 'Tháng', 'number'], ['year', 'Năm', 'number'], ['electricityPrice', 'Đơn giá điện (₫/kWh)', 'number'],
      ['waterPrice', 'Đơn giá nước (₫/m³)', 'number'], ['electricityStartIndex', 'Chỉ số điện đầu kỳ (kWh)', 'number'],
      ['electricityEndIndex', 'Chỉ số điện cuối kỳ (kWh)', 'number'], ['waterStartIndex', 'Chỉ số nước đầu kỳ (m³)', 'number'],
      ['waterEndIndex', 'Chỉ số nước cuối kỳ (m³)', 'number'], ['roomId', 'Mã phòng', 'text'],
    ],
  },
  {
    key: 'incidents', title: 'Sự cố', singular: 'sự cố', icon: 'incident', path: '/api/incidents', id: 'incidentId',
    columns: ['incidentId', 'incidentContent', 'reportDate', 'status', 'roomId'],
    fields: [
      ['incidentId', 'Mã sự cố', 'text'], ['incidentContent', 'Nội dung sự cố', 'textarea'], ['reportDate', 'Ngày báo', 'date'],
      ['status', 'Trạng thái', 'text'], ['roomId', 'Mã phòng', 'text'],
    ],
  },
  {
    key: 'incident-resolutions', title: 'Xử lý sự cố', singular: 'lần xử lý', icon: 'check', path: '/api/incident-resolutions', id: 'resolutionId',
    columns: ['resolutionId', 'resolutionDate', 'result', 'isCompleted', 'incidentId', 'employeeId'],
    fields: [
      ['resolutionId', 'Mã xử lý', 'text'], ['resolutionDate', 'Ngày xử lý', 'date'], ['result', 'Kết quả xử lý', 'textarea'],
      ['isCompleted', 'Đã hoàn tất', 'checkbox'], ['incidentId', 'Mã sự cố', 'text'], ['employeeId', 'Mã nhân viên', 'text'],
    ],
  },
  {
    key: 'cleaning-supports', title: 'Hỗ trợ dọn dẹp', singular: 'yêu cầu hỗ trợ', icon: 'sparkle', path: '/api/cleaning-supports', id: 'supportId',
    columns: ['supportId', 'supportDate', 'taskContent', 'isCompleted', 'roomId', 'employeeId'],
    fields: [
      ['supportId', 'Mã hỗ trợ', 'text'], ['supportDate', 'Ngày hỗ trợ', 'date'], ['taskContent', 'Nội dung công việc', 'textarea'],
      ['isCompleted', 'Đã hoàn tất', 'checkbox'], ['roomId', 'Mã phòng', 'text'], ['employeeId', 'Mã nhân viên', 'text'],
    ],
  },
  {
    key: 'rules', title: 'Nội quy', singular: 'nội quy', icon: 'rules', path: '/api/rules', id: 'ruleId',
    columns: ['ruleId', 'violationName', 'description', 'prescribedFine'],
    fields: [['ruleId', 'Mã nội quy', 'text'], ['violationName', 'Tên lỗi', 'text'], ['description', 'Mô tả', 'textarea'], ['prescribedFine', 'Mức phạt quy định', 'number']],
  },
  {
    key: 'violations', title: 'Vi phạm', singular: 'vi phạm', icon: 'violation', path: '/api/violations', id: 'violationId',
    columns: ['violationId', 'violationName', 'violationDate', 'roomId', 'ruleId', 'actualFine'],
    fields: [
      ['violationId', 'Mã vi phạm', 'text'], ['violationName', 'Tên vi phạm', 'text'], ['violationDate', 'Ngày vi phạm', 'date'],
      ['roomId', 'Mã phòng', 'text'], ['ruleId', 'Mã nội quy', 'text'], ['detailedDescription', 'Mô tả chi tiết', 'textarea'], ['actualFine', 'Tiền phạt thực tế', 'number'],
    ],
  },
]

export const relationMap = {
  tenantName: ['tenant','fullName'], tenantBirth: ['tenant','dateOfBirth'], tenantPhone: ['tenant','phoneNumber'],
  roomId: ['room', 'roomId'], tenantId: ['tenant', 'tenantId'], contractId: ['contract', 'contractId'],
  employeeId: ['employee', 'employeeId'], incidentId: ['incident', 'incidentId'], ruleId: ['rule', 'ruleId'],
}

export const labels = {
  actualCheckIn:'Nhận phòng thực tế',actualCheckOut:'Trả phòng thực tế',agreedRent:'Giá thuê thỏa thuận (₫/tháng)',
  tenantName: 'Khách thuê', tenantBirth: 'Ngày sinh', tenantPhone: 'Điện thoại',
  tenantId: 'Mã khách thuê', employeeId: 'Mã nhân viên', roomId: 'Mã phòng', contractId: 'Mã hợp đồng', invoiceId: 'Mã hóa đơn',
  readingId: 'Mã chỉ số', incidentId: 'Mã sự cố', resolutionId: 'Mã xử lý', supportId: 'Mã hỗ trợ', ruleId: 'Mã nội quy', violationId: 'Mã vi phạm',
  fullName: 'Họ tên', cccd: 'CCCD', phoneNumber: 'Điện thoại', dateOfBirth: 'Ngày sinh', permanentAddress: 'Địa chỉ', position: 'Vị trí',
  baseSalary: 'Lương cơ bản', seniority: 'Thâm niên', floor: 'Tầng', area: 'Diện tích (m²)', roomType: 'Loại phòng', status: 'Trạng thái',
  startDate: 'Bắt đầu', endDate: 'Kết thúc', deposit: 'Tiền cọc', month: 'Tháng', year: 'Năm', rentAmount: 'Tiền thuê', electricityAmount: 'Tiền điện',
  waterAmount: 'Tiền nước', fineAmount: 'Tiền phạt', totalAmount: 'Tổng tiền', electricityPrice: 'Đơn giá điện (₫/kWh)', waterPrice: 'Đơn giá nước (₫/m³)',
  electricityUsage:'Điện sử dụng (kWh)',waterUsage:'Nước sử dụng (m³ / khối)',electricityStartIndex: 'Điện đầu kỳ (kWh)', electricityEndIndex: 'Điện cuối kỳ (kWh)', waterStartIndex: 'Nước đầu kỳ (m³)', waterEndIndex: 'Nước cuối kỳ (m³)',
  incidentContent: 'Nội dung', reportDate: 'Ngày báo', resolutionDate: 'Ngày xử lý', result: 'Kết quả', isCompleted: 'Hoàn tất',
  supportDate: 'Ngày hỗ trợ', taskContent: 'Công việc', violationName: 'Tên lỗi', description: 'Mô tả', prescribedFine: 'Mức phạt',
  violationDate: 'Ngày vi phạm', detailedDescription: 'Chi tiết', actualFine: 'Phạt thực tế',
}

export function getValue(record, key) {
  if(key==='electricityUsage')return record?.electricityEndIndex!=null&&record?.electricityStartIndex!=null?record.electricityEndIndex-record.electricityStartIndex:''
  if(key==='waterUsage')return record?.waterEndIndex!=null&&record?.waterStartIndex!=null?record.waterEndIndex-record.waterStartIndex:''
  if (record?.[key] != null && typeof record[key] !== 'object') return record[key]
  const relation = relationMap[key]
  return relation ? record?.[relation[0]]?.[relation[1]] ?? '' : record?.[key] ?? ''
}
