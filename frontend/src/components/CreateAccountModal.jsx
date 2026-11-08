import { useState } from 'react';
import { BancoApi } from '../services/api';

export default function CreateAccountModal({ onClose, onSuccess }) {
  const [numeroCuenta, setNumeroCuenta] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const cuentaNum = numeroCuenta.trim().toUpperCase();
    if (!cuentaNum) {
      setError('Ingresá un número de cuenta');
      return;
    }

    setLoading(true);
    try {
      const creada = await BancoApi.crearCuenta(cuentaNum);
      onSuccess(`Cuenta ${creada.numeroCuenta} creada exitosamente con saldo inicial cero.`);
      onClose();
    } catch (err) {
      setError(err.message || 'Error al crear cuenta');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3>Crear nueva cuenta</h3>
          <button className="btn btn-secondary" style={{ padding: '4px 8px' }} onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="subtitle">Se creará a tu nombre, en pesos argentinos y con saldo inicial cero.</p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="numeroCuenta">Identificador de cuenta</label>
            <input
              id="numeroCuenta"
              type="text"
              maxLength={20}
              value={numeroCuenta}
              onChange={(e) => setNumeroCuenta(e.target.value)}
              placeholder="Ej: CA-ARS-002"
              required
              autoFocus
            />
            <small style={{ color: 'var(--text-dim)', fontSize: '0.8rem' }}>
              Hasta 20 caracteres alfanuméricos o guiones.
            </small>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creando...' : 'Crear Cuenta'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
