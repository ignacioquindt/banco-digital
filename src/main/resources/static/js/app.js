"use strict";
const $=id=>document.getElementById(id);
let perfil,cuentas=[],movimientos=[],pendiente=null,borrador=null,comprobanteActual=null;
function centavos(valor){const [entero,fraccion='']=String(valor).split('.');return BigInt(entero)*100n+BigInt(fraccion.padEnd(2,'0').slice(0,2));}
function mostrarCentavos(valor){return '$ '+(valor/100n).toLocaleString('es-AR')+','+(valor%100n).toString().padStart(2,'0');}
const dinero=valor=>mostrarCentavos(centavos(valor));
const fecha=valor=>new Date(valor).toLocaleString('es-AR',{timeZone:'America/Argentina/Buenos_Aires',hourCycle:'h23'});
function aviso(texto,error=false){$('mensaje').textContent=texto;$('mensaje').className='notice'+(error?' error':'');}
function texto(tag,contenido,clase){const e=document.createElement(tag);e.textContent=contenido;if(clase)e.className=clase;return e;}
function boton(etiqueta,accion,clase='secondary'){const e=texto('button',etiqueta,clase);e.type='button';e.addEventListener('click',accion);return e;}
function mostrar(vista){
    if(!['inicio','cuentas','transferir','movimientos','perfil'].includes(vista))vista='inicio';
    document.querySelectorAll('.section').forEach(e=>e.hidden=e.id!==vista);
    document.querySelectorAll('[data-view]').forEach(e=>e.classList.toggle('active',e.dataset.view===vista));
    history.replaceState(null,'','#'+vista);
}
function esMia(id){return cuentas.some(c=>c.id===id);}
function tipo(t){return esMia(t.origenId)&&esMia(t.destinoId)?'propia':esMia(t.origenId)?'salida':'entrada';}
function cuentaEtiqueta(id){const c=cuentas.find(c=>c.id===id);return c?c.numeroCuenta+' · ID '+id:'Cuenta ID '+id;}
function tabla(datos,contenedor){
    contenedor.replaceChildren();
    if(!datos.length){contenedor.append(texto('p','No hay movimientos para mostrar.','empty'));return;}
    const envoltura=document.createElement('div');envoltura.className='table-wrap';
    const t=document.createElement('table'),head=document.createElement('thead'),hr=document.createElement('tr');
    for(const label of ['Fecha (Buenos Aires)','Tipo','Origen','Destino','Importe',''])hr.append(texto('th',label));
    head.append(hr);t.append(head);const body=document.createElement('tbody');
    for(const m of datos){
        const row=document.createElement('tr');row.append(texto('td',fecha(m.fecha)));
        const celda=document.createElement('td'),clase=tipo(m);
        celda.append(texto('span',clase==='propia'?'Entre mis cuentas':clase==='salida'?'Enviada':'Recibida','pill '+(clase==='entrada'?'in':clase==='salida'?'out':'')));row.append(celda);
        row.append(texto('td',cuentaEtiqueta(m.origenId)),texto('td',cuentaEtiqueta(m.destinoId)),texto('td',dinero(m.importe)));
        const accion=document.createElement('td');accion.append(boton('Comprobante',()=>verComprobante(m),'outline'));row.append(accion);body.append(row);
    }
    t.append(body);envoltura.append(t);contenedor.append(envoltura);
}
function filtrar(){
    const cuenta=$('filtroCuenta').value,tipoElegido=$('filtroTipo').value,desde=$('desde').value,hasta=$('hasta').value;
    return movimientos.filter(m=>{
        const d=new Intl.DateTimeFormat('en-CA',{timeZone:'America/Argentina/Buenos_Aires',year:'numeric',month:'2-digit',day:'2-digit'}).format(new Date(m.fecha));
        return (!cuenta||m.origenId===Number(cuenta)||m.destinoId===Number(cuenta))&&(!tipoElegido||tipo(m)===tipoElegido)&&(!desde||d>=desde)&&(!hasta||d<=hasta);
    });
}
function pintar(){
    $('cantidadCuentas').textContent=cuentas.length;$('cantidadMovimientos').textContent=movimientos.length;
    $('saldoTotal').textContent=mostrarCentavos(cuentas.reduce((s,c)=>s+centavos(c.saldo),0n));
    const lista=$('listaCuentas');lista.replaceChildren();
    if(!cuentas.length)lista.append(texto('p','Todavía no tenés cuentas. Creá tu primera cuenta para comenzar.','empty'));
    for(const c of cuentas){
        const card=document.createElement('article');card.className='card';
        card.append(texto('div','CUENTA EN '+c.moneda,'label'),texto('h2',c.numeroCuenta),texto('p','ID de cuenta: '+c.id,'muted'),texto('div',dinero(c.saldo),'balance'));
        const acciones=document.createElement('div');acciones.className='card-actions';
        acciones.append(boton('Movimientos',()=>{$('filtroCuenta').value=String(c.id);tabla(filtrar(),$('historial'));mostrar('movimientos');}),boton('Transferir',()=>{$('origen').value=String(c.id);saldoOrigen();mostrar('transferir');},''));
        card.append(acciones);lista.append(card);
    }
    for(const id of ['origen','filtroCuenta']){
        const select=$(id),anterior=select.value;select.replaceChildren();
        if(id==='filtroCuenta'){const op=texto('option','Todas mis cuentas');op.value='';select.append(op);}
        for(const c of cuentas){const op=texto('option',c.numeroCuenta+' · ID '+c.id);op.value=c.id;select.append(op);}
        if([...select.options].some(o=>o.value===anterior))select.value=anterior;
    }
    $('revisarTransferencia').disabled=!cuentas.length||!!pendiente;
    $('pendiente').hidden=!pendiente;saldoOrigen();tabla(movimientos.slice(0,5),$('ultimos'));tabla(filtrar(),$('historial'));
}
function saldoOrigen(){const c=cuentas.find(c=>c.id===Number($('origen').value));$('saldoOrigen').textContent=c?'Saldo disponible: '+dinero(c.saldo):'Creá una cuenta para operar.';}
async function actualizar(){[cuentas,movimientos]=await Promise.all([BancoAPI.pedir('/api/cuentas'),BancoAPI.pedir('/api/transferencias')]);pintar();}
function claveAlmacenamiento(){return 'banco.transferencia.pendiente.'+perfil.id;}
function guardarPendiente(valor){
    // Persistir ANTES de enviar: un fallo de almacenamiento impide el envío.
    if(valor)sessionStorage.setItem(claveAlmacenamiento(),JSON.stringify(valor));else sessionStorage.removeItem(claveAlmacenamiento());
    pendiente=valor;$('pendiente').hidden=!valor;$('revisarTransferencia').disabled=!!valor||!cuentas.length;
}
function decimal(valor){const limpio=valor.trim().replace(',','.');if(!/^\d{1,17}(\.\d{1,2})?$/.test(limpio)||!/[1-9]/.test(limpio))throw new Error('Ingresá un importe positivo con hasta dos decimales, sin separadores de miles.');return limpio;}
function idValido(valor){if(!/^[1-9]\d*$/.test(valor)||!Number.isSafeInteger(Number(valor)))throw new Error('El ID de destino debe ser un entero positivo válido.');return Number(valor);}
function resumen(contenedor,pares){contenedor.replaceChildren();for(const [etiqueta,valor] of pares){const fila=document.createElement('div');fila.className='receipt-line';fila.append(texto('span',etiqueta),texto('strong',valor));contenedor.append(fila);}}
$('transferenciaForm').addEventListener('submit',e=>{
    e.preventDefault();aviso('');
    try{
        if(pendiente)throw new Error('Primero resolvé la transferencia pendiente.');
        const origenId=Number($('origen').value),destinoId=idValido($('destino').value.trim()),importe=decimal($('importe').value);
        if(!esMia(origenId))throw new Error('Seleccioná una cuenta de origen.');
        if(origenId===destinoId)throw new Error('Las cuentas deben ser distintas.');
        borrador={origenId,destinoId,importe,claveIdempotencia:crypto.randomUUID()};
        resumen($('resumenTransferencia'),[['Desde',cuentaEtiqueta(origenId)],['Hacia','Cuenta ID '+destinoId],['Importe',dinero(importe)]]);
        $('confirmarDialog').showModal();
    }catch(error){aviso(error.message,true);}
});
let enviando=false;
async function enviarTransferencia(){
    if(enviando)return;enviando=true;$('confirmarTransferencia').disabled=true;$('reintentar').disabled=true;$('cancelarTransferencia').disabled=true;
    try{
        if(!pendiente)guardarPendiente(borrador);
        const operacion=await BancoAPI.pedir('/api/transferencias',{method:'POST',body:pendiente});
        guardarPendiente(null);$('confirmarDialog').close();$('transferenciaForm').reset();
        aviso('Transferencia registrada correctamente.');verComprobante(operacion);
        try{await actualizar();}catch(error){aviso('La transferencia fue registrada, pero no pudimos actualizar los saldos. Recargá el panel.',true);}
    }catch(error){
        // Una respuesta de validación confirma el rechazo. Un fallo de red o 5xx es incierto.
        if([400,401,403,404,409].includes(error.status))guardarPendiente(null);
        $('confirmarDialog').close();aviso(error.message+(pendiente?' Reintentá desde la operación pendiente; conservamos la misma clave.':''),true);
    }finally{enviando=false;$('confirmarTransferencia').disabled=false;$('reintentar').disabled=false;$('cancelarTransferencia').disabled=false;}
}
$('confirmarTransferencia').addEventListener('click',enviarTransferencia);
$('reintentar').addEventListener('click',enviarTransferencia);
$('confirmarDialog').addEventListener('cancel',e=>{if(enviando)e.preventDefault();});
function verComprobante(m){comprobanteActual=m;resumen($('comprobante'),[['Operación','#'+m.id],['Fecha',fecha(m.fecha)],['Origen',cuentaEtiqueta(m.origenId)],['Destino',cuentaEtiqueta(m.destinoId)],['Importe',dinero(m.importe)]]);$('comprobante').append(texto('p','Referencia: '+m.claveIdempotencia,'receipt-id muted'));$('comprobanteDialog').showModal();}
function descargar(nombre,contenido,tipo){const url=URL.createObjectURL(new Blob([contenido],{type:tipo}));const a=document.createElement('a');a.href=url;a.download=nombre;a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);}
$('descargarComprobante').addEventListener('click',()=>{const m=comprobanteActual;descargar('comprobante-'+m.id+'.txt',['BANCO DIGITAL · SIMULADOR EDUCATIVO','Dinero ficticio','Operación: '+m.id,'Fecha (Buenos Aires): '+fecha(m.fecha),'Origen: '+cuentaEtiqueta(m.origenId),'Destino: '+cuentaEtiqueta(m.destinoId),'Importe: '+dinero(m.importe),'Referencia: '+m.claveIdempotencia].join('\r\n'),'text/plain;charset=utf-8');});
$('abrirCuenta').addEventListener('click',()=>{$('cuentaError').textContent='';$('cuentaDialog').showModal();});
$('cuentaForm').addEventListener('submit',async e=>{
    e.preventDefault();$('crearCuenta').disabled=true;$('cuentaError').textContent='';
    try{
        await BancoAPI.pedir('/api/cuentas',{method:'POST',body:{numeroCuenta:$('numeroCuenta').value.trim()}});
        $('cuentaDialog').close();$('cuentaForm').reset();aviso('Cuenta creada a tu nombre, con saldo cero.');
        try{await actualizar();}catch(error){aviso('La cuenta fue creada. Recargá el panel para actualizar el listado.',true);}
    }catch(error){$('cuentaError').textContent=error.message;}finally{$('crearCuenta').disabled=false;}
});
$('perfilForm').addEventListener('submit',async e=>{
    e.preventDefault();const boton=e.submitter;boton.disabled=true;
    try{perfil=await BancoAPI.pedir('/api/perfil',{method:'PUT',body:{nombre:$('nombrePerfil').value.trim()}});$('saludo').textContent='Hola, '+perfil.nombre;aviso('Nombre actualizado.');}catch(error){aviso(error.message,true);}finally{boton.disabled=false;}
});
$('passwordForm').addEventListener('submit',async e=>{
    e.preventDefault();const boton=e.submitter;boton.disabled=true;
    try{
        const nueva=$('passwordNueva').value;if(nueva!==$('passwordConfirmacion').value)throw new Error('Las contraseñas nuevas no coinciden.');BancoAPI.validarPassword(nueva);
        await BancoAPI.pedir('/api/perfil/password',{method:'POST',body:{passwordActual:$('passwordActual').value,passwordNueva:nueva}});
        location.href='/login.html?password=1';
    }catch(error){aviso(error.message,true);}finally{boton.disabled=false;}
});
$('salir').addEventListener('click',async e=>{
    const boton=e.currentTarget;boton.disabled=true;
    try{const csrf=await BancoAPI.csrf();const r=await fetch('/logout',{method:'POST',headers:{[csrf.headerName]:csrf.token}});if(!r.ok)throw new Error('No se pudo cerrar sesión.');location.href='/login.html?salida=1';}catch(error){aviso(error.message,true);boton.disabled=false;}
});
$('exportar').addEventListener('click',()=>{
    const filas=[['ID','Fecha Buenos Aires','Origen ID','Destino ID','Importe ARS'],...filtrar().map(m=>[m.id,fecha(m.fecha),m.origenId,m.destinoId,m.importe])];
    descargar('movimientos.csv','\uFEFF'+filas.map(f=>f.map(v=>'"'+String(v).replaceAll('"','""')+'"').join(';')).join('\r\n'),'text/csv;charset=utf-8');
});
document.querySelectorAll('[data-view]').forEach(b=>b.addEventListener('click',()=>mostrar(b.dataset.view)));
document.querySelectorAll('[data-go]').forEach(b=>b.addEventListener('click',()=>mostrar(b.dataset.go)));
document.querySelectorAll('[data-close]').forEach(b=>b.addEventListener('click',()=>$(b.dataset.close).close()));
for(const id of ['filtroCuenta','filtroTipo','desde','hasta'])$(id).addEventListener('change',()=>{if($('desde').value&&$('hasta').value&&$('desde').value>$('hasta').value){aviso('La fecha inicial no puede ser posterior a la final.',true);return;}aviso('');tabla(filtrar(),$('historial'));});
$('origen').addEventListener('change',saldoOrigen);
async function iniciar(){
    aviso('Cargando tus datos...');
    try{
        perfil=await BancoAPI.pedir('/api/perfil');$('saludo').textContent='Hola, '+perfil.nombre;$('nombrePerfil').value=perfil.nombre;$('emailPerfil').value=perfil.email;
        pendiente=JSON.parse(sessionStorage.getItem(claveAlmacenamiento())||'null');
        await actualizar();mostrar(location.hash.slice(1)||'inicio');aviso('');
    }catch(error){aviso('No pudimos cargar el panel. '+error.message,true);}
}
iniciar();
