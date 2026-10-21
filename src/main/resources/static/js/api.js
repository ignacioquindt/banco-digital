
"use strict";
window.BancoAPI = {
    async csrf() {
        const r=await fetch('/api/auth/csrf',{headers:{Accept:'application/json'},cache:'no-store'});
        if(!r.ok) throw new Error('No se pudo obtener el token de seguridad. Recargá la página.');
        return r.json();
    },
    async pedir(url, opciones={}) {
        const headers={Accept:'application/json',...opciones.headers};
        const method=opciones.method||'GET';
        if(!['GET','HEAD'].includes(method)) {
            const token=await this.csrf(); headers[token.headerName]=token.token;
        }
        if(opciones.body!==undefined) headers['Content-Type']='application/json';
        const r=await fetch(url,{...opciones,headers,cache:'no-store',
            body:opciones.body===undefined?undefined:JSON.stringify(opciones.body)});
        if(r.status===401 || (r.redirected && new URL(r.url).pathname.includes('login'))) {
            location.href='/login.html?expirada=1';
            throw new Error('La sesión venció. Iniciá sesión nuevamente.');
        }
        const data=await r.json().catch(()=>null);
        if(!r.ok) {
            const e=new Error(data?.mensaje || (r.status===403 ? 'La sesión o el token de seguridad vencieron. Recargá la página.' : 'No se pudo completar la operación.'));
            e.status=r.status;
            if(data?.campos) e.message+='\n'+Object.values(data.campos).join(' · ');
            throw e;
        }
        return data;
    },
    validarPassword(password) {
        if(password.length<12||password.length>64||new TextEncoder().encode(password).length>72)
            throw new Error('La contraseña debe tener de 12 a 64 caracteres y hasta 72 bytes UTF-8.');
    }
};
