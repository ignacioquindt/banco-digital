
"use strict";
const mensaje=document.getElementById('authMensaje');
function avisar(texto,error=false){mensaje.textContent=texto;mensaje.className='notice'+(error?' error':'');}
const parametros=new URLSearchParams(location.search);
if(parametros.has('error')) avisar('Correo o contraseña incorrectos.',true);
else if(parametros.has('salida')) avisar('Cerraste sesión correctamente.');
else if(parametros.has('expirada')) avisar('Tu sesión finalizó. Iniciá sesión nuevamente.');
else if(parametros.has('registro')) avisar('Usuario creado. Ya podés iniciar sesión.');
else if(parametros.has('password')) avisar('Contraseña actualizada. Iniciá sesión con la nueva.');
const login=document.getElementById('loginForm');
if(login){
    BancoAPI.csrf().then(token=>{
        const campo=document.getElementById('csrfLogin');campo.name=token.parameterName;campo.value=token.token;
        document.getElementById('submitAuth').disabled=false;
    }).catch(e=>avisar(e.message,true));
    login.addEventListener('submit',async event=>{
        event.preventDefault();const boton=document.getElementById('submitAuth');boton.disabled=true;
        try{
            const r=await fetch('/login',{method:'POST',body:new URLSearchParams(new FormData(login))});
            if(r.status===429){const d=await r.json();throw new Error(d.mensaje);}
            if(r.status===403)throw new Error('El token de seguridad venció. Recargá el formulario.');
            if(!r.ok)throw new Error('No se pudo iniciar sesión. Intentá nuevamente.');
            if(new URL(r.url).pathname.includes('login'))throw new Error('Correo o contraseña incorrectos.');
            location.href='/index.html';
        }catch(error){avisar(error.message,true);}finally{boton.disabled=false;}
    });
}
const registro=document.getElementById('registroForm');
const recuperar=document.getElementById('recuperarForm');
const restablecer=document.getElementById('restablecerForm');
let tokenRestablecer=new URLSearchParams(location.hash.slice(1)).get('token');
if(restablecer){history.replaceState(null,'',location.pathname);if(!tokenRestablecer){avisar('Abrí el enlace que recibiste por correo.',true);document.getElementById('submitAuth').disabled=true;}}
for(const form of [registro,recuperar,restablecer].filter(Boolean)) form.addEventListener('submit',async e=>{
    e.preventDefault();const boton=document.getElementById('submitAuth');boton.disabled=true;
    try{
        const datos=Object.fromEntries(new FormData(form));
        if(form===recuperar){
            const r=await BancoAPI.pedir('/api/auth/recuperar',{method:'POST',body:{email:datos.email.trim()}});avisar(r.mensaje);
        }else{
            if(datos.password!==datos.confirmacion) throw new Error('Las contraseñas no coinciden.');
            BancoAPI.validarPassword(datos.password);
            if(form===registro){
                await BancoAPI.pedir('/api/auth/registro',{method:'POST',body:{nombre:datos.nombre.trim(),email:datos.email.trim().toLowerCase(),password:datos.password}});
                location.href='/login.html?registro=1';
            }else{
                await BancoAPI.pedir('/api/auth/restablecer',{method:'POST',body:{token:tokenRestablecer,password:datos.password}});
                location.href='/login.html?password=1';
            }
        }
    }catch(error){avisar(error.message,true);}finally{boton.disabled=false;}
});
