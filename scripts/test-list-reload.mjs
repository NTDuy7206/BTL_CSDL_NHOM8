import assert from 'node:assert/strict'
import {createRequire} from 'node:module'
import {fileURLToPath,pathToFileURL} from 'node:url'
import {createServer} from '../frontend/node_modules/vite/dist/node/index.js'
import {restorePage,rememberPage} from '../frontend/src/data/sessionNavigation.js'
import {requireRows} from '../frontend/src/data/listResponse.js'
const require=createRequire(new URL('../frontend/package.json',import.meta.url));const React=require('react');const {renderToStaticMarkup}=require('react-dom/server');
const memory=new Map(),storage={getItem:k=>memory.get(k)??null,setItem:(k,v)=>memory.set(k,v)};const user={id:1,role:'USER'},admin={id:2,role:'ADMIN'};
assert.equal(rememberPage(storage,user,'contracts'),'contracts');assert.equal(restorePage(storage,user),'contracts');assert.equal(restorePage(storage,{id:3,role:'USER'}),'dashboard');assert.equal(rememberPage(storage,user,'accounts'),'dashboard');assert.equal(rememberPage(storage,admin,'accounts'),'accounts');assert.equal(restorePage(storage,admin),'accounts');assert.equal(rememberPage(storage,admin,'new-rental'),'dashboard');
const rows=[{ruleId:'NQ01',violationName:'Giữ vệ sinh'}];assert.equal(requireRows(rows),rows);for(const malformed of [null,{},'<html>error</html>',[null],[1],[[]]])assert.throws(()=>requireRows(malformed));
const server=await createServer({root:fileURLToPath(new URL('../frontend',import.meta.url)),configFile:fileURLToPath(new URL('../frontend/vite.config.js',import.meta.url)),server:{middlewareMode:true},appType:'custom'});
try{
 const {default:QueryPanel}=await server.ssrLoadModule('/src/components/QueryPanel.jsx');const {default:DataTable}=await server.ssrLoadModule('/src/components/DataTable.jsx');const module={key:'rules',title:'Nội quy',singular:'nội quy',path:'/api/rules',id:'ruleId',columns:['ruleId','violationName']};const base={module,request:async()=>[],readOnly:true};
 const search=renderToStaticMarkup(React.createElement(QueryPanel,{...base,keyword:'',onKeywordChange:()=>{},onResults:()=>{}}));assert.ok(search.includes('type="search"'));assert.ok(search.includes('Tìm kiếm theo từ khóa'));
 for(const state of [{rows:[],loading:true},{rows:[],loading:false},{rows:[],error:'API error'},{rows,error:'API error'}]){const html=renderToStaticMarkup(React.createElement(DataTable,{module,...state}));assert.ok(html.includes('Tìm theo từ khóa'),'search toolbar must stay visible');if(state.rows===rows)assert.ok(html.includes('NQ01'),'last loaded rows survive a refresh error');}
 for(const[name,props]of [['AccountsPage',{}],['ContractRequestsPage',{role:'ADMIN'}],['PaymentsPage',{role:'ADMIN'}]]){const {default:Component}=await server.ssrLoadModule('/src/pages/'+name+'.jsx');const html=renderToStaticMarkup(React.createElement(Component,{request:async()=>[],notify:()=>{},...props}));assert.ok(html.includes('type="search"'),name+' search must exist even when empty');}
 const {modules}=await server.ssrLoadModule('/src/data/modules.js');const roomModule=modules.find(m=>m.key==='rooms');
 const ownedRooms=[{roomId:'L1-101',landlordName:'Chủ A',roomType:'Studio'},{roomId:'L2-101',landlordName:'Chủ B',roomType:'Studio'}];
 const roomHtml=renderToStaticMarkup(React.createElement(DataTable,{module:roomModule,rows:ownedRooms,readOnly:true}));assert.ok(roomHtml.includes('Chủ nhà'));assert.ok(roomHtml.includes('Chủ A'));assert.ok(roomHtml.includes('Chủ B'));
 const filteredRooms=renderToStaticMarkup(React.createElement(DataTable,{module:roomModule,rows:ownedRooms,readOnly:true,searchValue:'chu b'}));assert.ok(filteredRooms.includes('L2-101'));assert.ok(!filteredRooms.includes('L1-101'));
 console.log('Reload navigation, list validation and 9 empty/error/search UI scenarios passed');
}finally{await server.close()}
