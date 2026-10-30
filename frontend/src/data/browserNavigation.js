import {validPage,restorePage,rememberPage} from './sessionNavigation.js'
export function pageHref(page,{roomId,apply=false}={}){const query=new URLSearchParams();if(roomId){query.set('room',roomId);if(apply)query.set('apply','1')}return '/#/'+encodeURIComponent(page)+(query.size?'?'+query.toString():'')}
export function roomRoute(hash){const params=new URLSearchParams(hash.split('?')[1]||'');const room=params.get('room');return {roomId:room&&room.length<=20?room:'',apply:params.get('apply')==='1'}}
export function pageFromHash(hash,user){
 if(!hash||hash==='#'||hash==='#/')return null
 try{return validPage(decodeURIComponent(hash.split('?')[0].replace(/^#\/?/,'')),user)}catch{return 'dashboard'}
}
export function initialPage(browser,storage,user){return pageFromHash(browser.location.hash,user)??restorePage(storage,user)}
export function visitPage(browser,storage,user,page,{replace=false,roomId,apply=false}={}){
 const next=rememberPage(storage,user,page),href=pageHref(next,{roomId,apply})
 if(browser.location.pathname+browser.location.search+browser.location.hash!==href)browser.history[replace?'replaceState':'pushState']({buildingPage:next},'',href)
 return next
}
export function followPageLink(event,onNavigate,page){
 if(event.defaultPrevented||event.button!==0||event.metaKey||event.ctrlKey||event.shiftKey||event.altKey)return
 event.preventDefault();onNavigate(page)
}
