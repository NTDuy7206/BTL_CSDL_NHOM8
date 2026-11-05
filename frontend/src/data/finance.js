export const todayVN=()=>new Intl.DateTimeFormat('sv-SE',{timeZone:'Asia/Ho_Chi_Minh'}).format(new Date())
export const money=value=>Number(value??0).toLocaleString('vi-VN',{maximumFractionDigits:2})+' ₫'
export const utilityLabel=type=>type==='ELECTRICITY'?'Tiền điện':'Tiền nước'
export function monthRange(month){const [year,m]=month.split('-').map(Number);const last=new Date(Date.UTC(year,m,0)).getUTCDate();return {from:month+'-01',to:month+'-'+String(last).padStart(2,'0')}}
export function shiftMonth(month,step){const [year,m]=month.split('-').map(Number);const date=new Date(Date.UTC(year,m-1+step,1));return date.toISOString().slice(0,7)}
export function reportRange(date,group){const year=date.slice(0,4);return group==='day'?monthRange(date.slice(0,7)):{from:group==='month'?year+'-01-01':String(Math.max(2000,Number(year)-4))+'-01-01',to:year+'-12-31'}}
