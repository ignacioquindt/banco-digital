import { useState } from 'react';
import { BancoApi } from '../services/api';

export default function CashOutModal({ cuenta, onClose, onSuccess }) {
  const [importe, setImporte] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const val = importe.trim().replace(/\./g, '').replace(',', '.');
    if (!val || Number(val) <= 0) {
      setError('Ingresá un importe válido mayor a cero');
      return;
    }

    setLoading(true);
    try {
      const claveIdempotencia = crypto.randomUUID();
      await BancoApi.extraer(cuenta.id, val, claveIdempotencia);
      onSuccess(`Se extrajeron $ ${val} ARS de la cuenta ${cuenta.numeroCuenta}`);
      onClose();
    } catch (err) {
      setError(err.message || 'Error al extraer dinero');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3>Extraer dinero (Cash-out)</h3>
          <button className="btn btn-secondary" style={{ padding: '4px 8px' }} onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="subtitle">
          Cuenta: <strong>{cuenta.numeroCuenta}</strong> · Saldo disponible:{' '}
          <strong style={{ color: 'var(--success)' }}>$ {Number(cuenta.saldo).toLocaleString('es-AR')} ARS</strong>
        </p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="importeExtraccion">Importe a retirar (ARS)</label>
            <input
              id="importeExtraccion"
              type="text"
              inputMode="decimal"
              value={importe}
              onChange={(e) => setImporte(e.target.value)}
              placeholder="Ej: 5000,00"
              required
              autoFocus
            />
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-danger" disabled={loading}>
              {loading ? 'Extrayendo...' : 'Confirmar Extracción'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
