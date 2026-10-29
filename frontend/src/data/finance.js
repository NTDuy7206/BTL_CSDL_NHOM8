export const todayVN=()=>new Date().toLocaleDateString('en-CA',{timeZone:'Asia/Ho_Chi_Minh'})
export const money=value=>Number(value??0).toLocaleString('vi-VN',{maximumFractionDigits:2})+' ₫'
export const utilityLabel=type=>type==='ELECTRICITY'?'Tiền điện':'Tiền nước'
export function reportRange(date,group){const year=date.slice(0,4);return{from:group==='day'?date.slice(0,7)+'-01':group==='month'?year+'-01-01':String(Math.max(2000,Number(year)-4))+'-01-01',to:date}}
