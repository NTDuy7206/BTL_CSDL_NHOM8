import {validPage,restorePage,rememberPage} from './sessionNavigation.js'
export function pageHref(page){return '/#/'+encodeURIComponent(page)}
export function pageFromHash(hash,user){
 if(!hash||hash==='#'||hash==='#/')return null
 try{return validPage(decodeURIComponent(hash.replace(/^#\/?/,'')),user)}catch{return 'dashboard'}
}
export function initialPage(browser,storage,user){return pageFromHash(browser.location.hash,user)??restorePage(storage,user)}
export function visitPage(browser,storage,user,page,{replace=false}={}){
 const next=rememberPage(storage,user,page),href=pageHref(next)
 if(browser.location.pathname+browser.location.search+browser.location.hash!==href)browser.history[replace?'replaceState':'pushState']({buildingPage:next},'',href)
 return next
}
export function followPageLink(event,onNavigate,page){
 if(event.defaultPrevented||event.button!==0||event.metaKey||event.ctrlKey||event.shiftKey||event.altKey)return
 event.preventDefault();onNavigate(page)
}
