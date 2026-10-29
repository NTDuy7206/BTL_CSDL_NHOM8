import{visibleModules}from './permissions.js'
function key(user){return 'buildingPage:'+String(user?.id??user?.username??'anonymous')}
export function validPage(page,user){const allowed=['dashboard','profile','payments','rental-requests',...visibleModules(user?.role).map(m=>m.key),...(user?.role==='ADMIN'?['accounts']:[])];return allowed.includes(page)?page:'dashboard'}
export function restorePage(storage,user){try{return validPage(storage.getItem(key(user)),user)}catch{return 'dashboard'}}
export function rememberPage(storage,user,page){const next=validPage(page,user);try{storage.setItem(key(user),next)}catch{}return next}
