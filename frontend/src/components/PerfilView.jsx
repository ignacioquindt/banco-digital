import { useState } from 'react';
import { BancoApi } from '../services/api';

export default function PerfilView({ perfil, onPerfilActualizado, onLogout }) {
  const [nombre, setNombre] = useState(perfil.nombre || '');
  const [guardandoNombre, setGuardandoNombre] = useState(false);
  const [mensajeNombre, setMensajeNombre] = useState(null);

  const [passwordActual, setPasswordActual] = useState('');
  const [passwordNueva, setPasswordNueva] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [guardandoPass, setGuardandoPass] = useState(false);
  const [mensajePass, setMensajePass] = useState(null);

  const handleActualizarNombre = async (e) => {
    e.preventDefault();
    setMensajeNombre(null);
    setGuardandoNombre(true);
    try {
      const res = await BancoApi.request('/api/perfil', {
        method: 'PUT',
        body: { nombre: nombre.trim() },
      });
      setMensajeNombre({ tipo: 'success', texto: 'Nombre actualizado correctamente' });
      if (onPerfilActualizado) onPerfilActualizado(res);
    } catch (err) {
      setMensajeNombre({ tipo: 'error', texto: err.message || 'Error al actualizar el nombre' });
    } finally {
      setGuardandoNombre(false);
    }
  };

  const handleCambiarPassword = async (e) => {
    e.preventDefault();
    setMensajePass(null);

    if (passwordNueva !== passwordConfirm) {
      setMensajePass({ tipo: 'error', texto: 'La nueva contraseña y su confirmación no coinciden' });
      return;
    }
    if (passwordNueva.length < 12) {
      setMensajePass({ tipo: 'error', texto: 'La contraseña debe tener al menos 12 caracteres' });
      return;
    }

    setGuardandoPass(true);
    try {
      await BancoApi.request('/api/perfil/password', {
        method: 'POST',
        body: { passwordActual, passwordNueva },
      });
      setMensajePass({ tipo: 'success', texto: 'Contraseña actualizada con éxito. Cerrando sesión...' });
      setTimeout(() => {
        if (onLogout) onLogout();
      }, 2000);
    } catch (err) {
      setMensajePass({ tipo: 'error', texto: err.message || 'Error al cambiar la contraseña' });
    } finally {
      setGuardandoPass(false);
    }
  };

  const esAdmin = perfil.rol === 'ROLE_ADMIN';

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <h1>Perfil de Usuario y Seguridad</h1>
        <p className="subtitle">Gestión de identidad, credenciales de acceso y permisos RBAC asignados.</p>
      </div>

      <div className="grid-cards" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', marginBottom: 24 }}>
        <div className="card">
          <span className="card-title">Rol Asignado (RBAC)</span>
          <div style={{ margin: '12px 0' }}>
            <span
              className="badge"
              style={{
                background: esAdmin ? 'rgba(59, 130, 246, 0.2)' : 'rgba(16, 185, 129, 0.2)',
                color: esAdmin ? '#60a5fa' : '#34d399',
                border: `1px solid ${esAdmin ? 'rgba(59, 130, 246, 0.5)' : 'rgba(16, 185, 129, 0.5)'}`,
                padding: '6px 14px',
                fontSize: '0.9rem',
                fontWeight: 700,
              }}
            >
              {esAdmin ? '🛡️ ADMINISTRADOR CORPORATIVO' : '💳 CLIENTE BANCARIO'}
            </span>
          </div>
          <small style={{ color: 'var(--text-muted)' }}>
            Permisos: {esAdmin ? 'Acceso pleno a auditoría y métricas globales' : 'Operatoria estándar sobre cuentas propias'}
          </small>
        </div>

        <div className="card">
          <span className="card-title">Identificador de Usuario</span>
          <div className="card-value" style={{ fontSize: '1.6rem', color: 'var(--text-primary)' }}>
            ID #{perfil.id}
          </div>
          <small style={{ color: 'var(--text-muted)', marginTop: 8 }}>Estado: Activo · Verificado</small>
        </div>

        <div className="card">
          <span className="card-title">Correo Electrónico Oficial</span>
          <div className="card-value" style={{ fontSize: '1.2rem', color: '#38bdf8', wordBreak: 'break-all' }}>
            {perfil.email}
          </div>
          <small style={{ color: 'var(--text-muted)', marginTop: 8 }}>Autenticación vía BCrypt Hash</small>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: 24 }}>
        {/* Modificar Datos Personales */}
        <div className="card">
          <h3 style={{ marginBottom: 16, color: 'var(--text-primary)' }}>Datos del Titular</h3>
          {mensajeNombre && (
            <div className={`alert alert-${mensajeNombre.tipo}`} style={{ marginBottom: 16 }}>
              {mensajeNombre.texto}
            </div>
          )}
          <form onSubmit={handleActualizarNombre}>
            <div className="form-group">
              <label htmlFor="perfilNombre">Nombre completo</label>
              <input
                id="perfilNombre"
                type="text"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                required
              />
            </div>
            <button type="submit" className="btn btn-primary" disabled={guardandoNombre}>
              {guardandoNombre ? 'Guardando...' : 'Guardar Cambios'}
            </button>
          </form>
        </div>

        {/* Cambiar Contraseña */}
        <div className="card">
          <h3 style={{ marginBottom: 16, color: 'var(--text-primary)' }}>Seguridad y Contraseña</h3>
          {mensajePass && (
            <div className={`alert alert-${mensajePass.tipo}`} style={{ marginBottom: 16 }}>
              {mensajePass.texto}
            </div>
          )}
          <form onSubmit={handleCambiarPassword}>
            <div className="form-group">
              <label htmlFor="passActual">Contraseña actual</label>
              <input
                id="passActual"
                type="password"
                value={passwordActual}
                onChange={(e) => setPasswordActual(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="passNueva">Nueva contraseña (mínimo 12 caracteres)</label>
              <input
                id="passNueva"
                type="password"
                value={passwordNueva}
                onChange={(e) => setPasswordNueva(e.target.value)}
                minLength={12}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="passConfirm">Confirmar nueva contraseña</label>
              <input
                id="passConfirm"
                type="password"
                value={passwordConfirm}
                onChange={(e) => setPasswordConfirm(e.target.value)}
                minLength={12}
                required
              />
            </div>
            <button type="submit" className="btn btn-secondary" disabled={guardandoPass}>
              {guardandoPass ? 'Actualizando...' : 'Actualizar Contraseña'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
