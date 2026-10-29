export const completionModules=new Set(['incidents','incident-resolutions','cleaning-supports'])
export function isComplete(module,row){if(module!=='incidents')return row.isCompleted===true;const s=String(row.status||'').normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/đ/g,'d').toUpperCase().trim();return ['COMPLETED','RESOLVED','DONE','DA XU LY','DA HOAN THANH','HOAN THANH'].includes(s)}
export function visibleWork(module,rows,view){return rows.filter(row=>isComplete(module,row)===(view==='completed'))}
