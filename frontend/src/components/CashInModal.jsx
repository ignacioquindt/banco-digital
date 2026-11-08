import { useState } from 'react';
import { BancoApi } from '../services/api';

export default function CashInModal({ cuenta, onClose, onSuccess }) {
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
      await BancoApi.depositar(cuenta.id, val, claveIdempotencia);
      onSuccess(`Se acreditaron $ ${val} ARS en la cuenta ${cuenta.numeroCuenta}`);
      onClose();
    } catch (err) {
      setError(err.message || 'Error al ingresar dinero');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3>Ingresar dinero (Cash-in)</h3>
          <button className="btn btn-secondary" style={{ padding: '4px 8px' }} onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="subtitle">
          Cuenta destino: <strong>{cuenta.numeroCuenta}</strong> (ID: {cuenta.id})
        </p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="importeDeposito">Importe a depositar (ARS)</label>
            <input
              id="importeDeposito"
              type="text"
              inputMode="decimal"
              value={importe}
              onChange={(e) => setImporte(e.target.value)}
              placeholder="Ej: 25000,00"
              required
              autoFocus
            />
            <small style={{ color: 'var(--text-dim)', fontSize: '0.8rem', marginTop: 4 }}>
              Operación atómica con clave de idempotencia UUID y registro en Transactional Outbox.
            </small>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-success" disabled={loading}>
              {loading ? 'Acreditando...' : 'Confirmar Depósito'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
