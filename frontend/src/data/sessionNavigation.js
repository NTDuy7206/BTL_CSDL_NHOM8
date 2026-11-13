import{visibleModules}from './permissions.js'
function key(user){return 'buildingPage:'+String(user?.id??user?.username??'anonymous')}
export function validPage(page,user){const role=user?.role;const allowed=role==='EMPLOYEE'?['dashboard','profile','staff-payroll','staff-applications']:['dashboard','profile','payments','rental-requests',...visibleModules(role).map(m=>m.key),...(role==='ADMIN'?['accounts','profits','staff-admin','staff-payroll','staff-applications']:[])];return allowed.includes(page)?page:'dashboard'}
export function restorePage(storage,user){try{return validPage(storage.getItem(key(user)),user)}catch{return 'dashboard'}}
export function rememberPage(storage,user,page){const next=validPage(page,user);try{storage.setItem(key(user),next)}catch{}return next}
