import { useState } from 'react';
import { BancoApi } from '../services/api';

export default function TransferModal({ cuentas, defaultOrigenId, onClose, onSuccess }) {
  const [origenId, setOrigenId] = useState(defaultOrigenId || (cuentas[0]?.id ? String(cuentas[0].id) : ''));
  const [destinoId, setDestinoId] = useState('');
  const [importe, setImporte] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const cuentaSeleccionada = cuentas.find((c) => String(c.id) === String(origenId));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!origenId) {
      setError('Seleccioná una cuenta de origen');
      return;
    }
    if (!destinoId || String(origenId) === String(destinoId)) {
      setError('La cuenta de destino debe ser distinta de la de origen');
      return;
    }

    const val = importe.trim().replace(/\./g, '').replace(',', '.');
    if (!val || Number(val) <= 0) {
      setError('Ingresá un importe válido mayor a cero');
      return;
    }

    setLoading(true);
    try {
      const claveIdempotencia = crypto.randomUUID();
      const res = await BancoApi.transferir(Number(origenId), Number(destinoId), val, claveIdempotencia);
      onSuccess(`Transferencia de $ ${val} ARS enviada exitosamente a cuenta #${destinoId}`, res);
      onClose();
    } catch (err) {
      setError(err.message || 'Error al ejecutar la transferencia');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3>Nueva transferencia</h3>
          <button className="btn btn-secondary" style={{ padding: '4px 8px' }} onClick={onClose}>
            ✕
          </button>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="cuentaOrigen">Cuenta de origen</label>
            <select
              id="cuentaOrigen"
              value={origenId}
              onChange={(e) => setOrigenId(e.target.value)}
              required
            >
              {cuentas.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.numeroCuenta} (Saldo: $ {Number(c.saldo).toLocaleString('es-AR')})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="cuentaDestino">ID de cuenta destino</label>
            <input
              id="cuentaDestino"
              type="text"
              inputMode="numeric"
              pattern="[0-9]+"
              value={destinoId}
              onChange={(e) => setDestinoId(e.target.value)}
              placeholder="Ej: 2"
              required
            />
            <small style={{ color: 'var(--text-dim)', fontSize: '0.8rem' }}>
              Ingresá el ID interno de la cuenta receptora.
            </small>
          </div>

          <div className="form-group">
            <label htmlFor="importeTransfer">Importe (ARS)</label>
            <input
              id="importeTransfer"
              type="text"
              inputMode="decimal"
              value={importe}
              onChange={(e) => setImporte(e.target.value)}
              placeholder="0,00"
              required
            />
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Transfiriendo...' : 'Confirmar Envío'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
