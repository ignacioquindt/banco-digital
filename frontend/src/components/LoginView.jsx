import { useState } from 'react';
import { BancoApi } from '../services/api';

export default function LoginView({ onLoginSuccess }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const perfil = await BancoApi.login(email.trim(), password);
      onLoginSuccess(perfil);
    } catch (err) {
      setError(err.message || 'Error al iniciar sesión');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <div className="brand" style={{ justifyContent: 'center', fontSize: '1.6rem' }}>
            Banco<span>Digital</span>
          </div>
          <div className="brand-badge">Simulador Bancario · SPA React</div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Correo electrónico</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="ejemplo@banco.local"
              required
              autoFocus
            />
          </div>

          <div className="form-group" style={{ marginBottom: 24 }}>
            <label htmlFor="password">Contraseña</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••••••"
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', padding: '12px' }}
            disabled={loading}
          >
            {loading ? 'Ingresando...' : 'Iniciar Sesión'}
          </button>
        </form>

        <p style={{ marginTop: 24, fontSize: '0.82rem', color: 'var(--text-muted)', textAlign: 'center' }}>
          Para registrar una cuenta nueva o recuperar contraseña, utilizá el formulario nativo del backend.
        </p>
      </div>
    </div>
  );
}
