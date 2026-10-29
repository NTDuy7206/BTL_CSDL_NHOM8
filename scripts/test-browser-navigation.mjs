import assert from 'node:assert/strict'
import {initialPage,pageFromHash,pageHref,visitPage,followPageLink} from '../frontend/src/data/browserNavigation.js'
const values=new Map(),storage={getItem:k=>values.get(k)??null,setItem:(k,v)=>values.set(k,v)}
const user={id:1,role:'USER'},admin={id:2,role:'ADMIN'}
let entries=[new URL('https://example.test/')],index=0
const browser={get location(){return entries[index]},history:{pushState(state,unused,path){entries=entries.slice(0,index+1);entries.push(new URL(path,entries[index]));index++},replaceState(state,unused,path){entries[index]=new URL(path,entries[index])}}}
assert.equal(initialPage(browser,storage,user),'dashboard')
visitPage(browser,storage,user,'dashboard',{replace:true});assert.equal(entries.length,1)
visitPage(browser,storage,user,'rooms');visitPage(browser,storage,user,'contracts');assert.equal(entries.length,3)
index--;assert.equal(initialPage(browser,storage,user),'rooms','Back resolves previous section')
index++;assert.equal(initialPage(browser,storage,user),'contracts','Forward resolves next section')
assert.equal(initialPage({location:new URL('https://example.test/#/invoices')},storage,user),'invoices','shared/direct URL overrides remembered page')
visitPage(browser,storage,user,'contracts');assert.equal(entries.length,3,'repeated selection must not add history entries')
visitPage(browser,storage,user,'accounts');assert.equal(browser.location.hash,'#/dashboard','USER cannot open admin page')
assert.equal(pageFromHash('#/accounts',admin),'accounts');assert.equal(pageFromHash('#/unknown',user),'dashboard');assert.equal(pageFromHash('#/%E0%A4%A',user),'dashboard')
assert.equal(pageHref('rental-requests'),'/#/rental-requests')
let called=0,prevented=0;const click={button:0,preventDefault(){prevented++}}
followPageLink(click,()=>called++,'rooms');assert.equal(called,1);assert.equal(prevented,1)
for(const extra of [{ctrlKey:true},{metaKey:true},{shiftKey:true},{altKey:true},{button:1},{defaultPrevented:true}])followPageLink({...click,...extra},()=>called++,'rooms')
assert.equal(called,1,'new tab / modified clicks retain native link behavior');assert.equal(prevented,1)
console.log('Browser navigation: Back/Forward, direct links, reload, role guards and normal link behavior passed')

assert.equal(pageFromHash('#/profits',user),'dashboard');assert.equal(pageFromHash('#/profits',admin),'profits');
