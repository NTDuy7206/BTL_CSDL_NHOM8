const statuses={AVAILABLE:'phòng trống còn trống',VACANT:'phòng trống',OCCUPIED:'đã thuê đang thuê',PENDING:'chờ xử lý đang chờ',COMPLETED:'đã xong đã xử lý hoàn thành',APPROVED:'đã duyệt',REJECTED:'từ chối cần kiểm tra lại',PAID:'đã thanh toán đã xác nhận',UNCONFIGURED:'chưa cấu hình'}
const aliases={roomId:'phòng',contractId:'hợp đồng',tenantId:'khách thuê',employeeId:'nhân viên',fullName:'họ tên',incidentId:'sự cố',invoiceId:'hóa đơn'}
export function normalizeKeyword(value){return String(value??'').normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/đ/g,'d').replace(/Đ/g,'D').toLocaleLowerCase('vi').replace(/\s+/g,' ').trim()}
function values(value,key=''){
 if(value==null)return []
 if(Array.isArray(value))return value.flatMap(item=>values(item))
 if(typeof value==='object')return Object.entries(value).filter(([k])=>!['password','accessToken','refreshToken','secret'].includes(k)).flatMap(([k,v])=>values(v,k))
 const result=[String(value)];if(aliases[key])result.push(aliases[key]+' '+value)
 if(typeof value==='number')result.push(value.toLocaleString('vi-VN'))
 if(typeof value==='boolean')result.push(value?'đã xong hoàn tất đã hoàn thành':'chưa xong chờ xử lý chưa hoàn thành')
 if(typeof value==='string'){if(statuses[value.toUpperCase()])result.push(statuses[value.toUpperCase()]);const date=value.match(/^(\d{4})-(\d{2})-(\d{2})(?:T|$)/);if(date)result.push(Number(date[3])+'/'+Number(date[2])+'/'+date[1],date[3]+'/'+date[2]+'/'+date[1])}
 return result
}
export function matchesKeyword(row,keyword){const terms=normalizeKeyword(keyword).split(' ').filter(Boolean);if(!terms.length)return true;const text=normalizeKeyword(values(row).join(' '));return terms.every(term=>text.includes(term))}
