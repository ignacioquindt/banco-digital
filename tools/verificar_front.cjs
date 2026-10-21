const {JSDOM}=require('jsdom');const fs=require('fs'),assert=require('assert/strict');
const base=require('path').resolve(__dirname,'../src/main/resources/static')+'/';
const dom=new JSDOM(fs.readFileSync(base+'index.html','utf8'),{url:'http://localhost:8080/index.html',runScripts:'outside-only'});const w=dom.window;
w.HTMLDialogElement.prototype.showModal=function(){this.open=true};w.HTMLDialogElement.prototype.close=function(){this.open=false};Object.defineProperty(w.crypto,'randomUUID',{value:()=>require('crypto').randomUUID()});w.URL.createObjectURL=()=> 'blob:test';w.URL.revokeObjectURL=()=>{};
let cuentas=[{id:1,numeroCuenta:'DEMO-001',moneda:'ARS',saldo:'1000.00'},{id:2,numeroCuenta:'DEMO-002',moneda:'ARS',saldo:'200.00'}],movimientos=[],envios=[],fallar=false;
w.BancoAPI={validarPassword:()=>{},pedir:async(url,opt={})=>{
 if(url==='/api/perfil')return{id:1,nombre:'Ignacio',email:'ignacio@example.com'};
 if(url==='/api/cuentas'&&opt.method==='POST'){cuentas.push({id:3,numeroCuenta:opt.body.numeroCuenta,moneda:'ARS',saldo:'0.00'});return cuentas.at(-1)}
 if(url==='/api/cuentas')return cuentas;
 if(url==='/api/transferencias'&&opt.method==='POST'){envios.push({...opt.body});if(fallar){fallar=false;throw new TypeError('Fallo de red')};const t={...opt.body,id:1,fecha:'2026-10-15T21:00:00Z'};movimientos=[t];return t;}
 if(url==='/api/transferencias')return movimientos;
 throw Error('Ruta inesperada '+url);
}};
w.eval(fs.readFileSync(base+'js/app.js','utf8'));const tick=()=>new Promise(r=>setTimeout(r,15));const el=id=>w.document.getElementById(id);
(async()=>{await tick();assert.equal(el('cantidadCuentas').textContent,'2');assert.equal(el('saldoTotal').textContent,'$ 1.200,00');
 el('abrirCuenta').click();assert(el('cuentaDialog').open);el('numeroCuenta').value='DEMO-003';el('cuentaForm').dispatchEvent(new w.Event('submit',{cancelable:true}));await tick();assert.equal(el('cantidadCuentas').textContent,'3');
 el('origen').value='1';el('destino').value='2';el('importe').value='100,00';el('transferenciaForm').dispatchEvent(new w.Event('submit',{cancelable:true}));assert(el('confirmarDialog').open);
 fallar=true;el('confirmarTransferencia').click();await tick();assert.equal(envios.length,1);assert.equal(el('pendiente').hidden,false);assert(el('revisarTransferencia').disabled);
 el('reintentar').click();await tick();assert.equal(envios.length,2);assert.equal(envios[0].claveIdempotencia,envios[1].claveIdempotencia);assert.equal(envios[1].importe,'100.00');assert(el('comprobanteDialog').open);assert.equal(el('cantidadMovimientos').textContent,'1');assert.equal(el('pendiente').hidden,true);
 assert(el('comprobante').textContent.includes('15/10/2026'));assert(el('comprobante').textContent.includes('18:00:00'));
 console.log('FRONT OK: carga, suma decimal, creación, confirmación, reintento idempotente, comprobante y fecha Buenos Aires');dom.window.close();})().catch(e=>{console.error(e);process.exit(1)});
