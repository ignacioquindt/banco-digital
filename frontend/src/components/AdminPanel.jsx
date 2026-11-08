import { useState, useEffect } from 'react';
import { BancoApi } from '../services/api';

export default function AdminPanel() {
  const [metricas, setMetricas] = useState(null);
  const [auditorias, setAuditorias] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const cargarDatos = async () => {
    setLoading(true);
    setError('');
    try {
      const [met, aud] = await Promise.all([
        BancoApi.getMetricasAdmin(),
        BancoApi.getAuditoriaAdmin(),
      ]);
      setMetricas(met);
      setAuditorias(aud);
    } catch (err) {
      setError(err.message || 'Error al cargar panel administrativo');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, []);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div>
          <h1>Panel de Administración Bancaria</h1>
          <p className="subtitle">Monitoreo global del sistema, métricas contables y auditoría en tiempo real.</p>
        </div>
        <button className="btn btn-secondary" onClick={cargarDatos} disabled={loading}>
          {loading ? 'Actualizando...' : 'Refrescar'}
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {metricas && (
        <div className="grid-cards">
          <div className="card">
            <span className="card-title">Dinero total en circulación</span>
            <div className="card-value" style={{ color: 'var(--success)' }}>
              $ {Number(metricas.saldoTotalEnCirculacion).toLocaleString('es-AR')}
            </div>
            <small style={{ color: 'var(--text-muted)', marginTop: 8 }}>Consolidado entre todas las cuentas</small>
          </div>

          <div className="card">
            <span className="card-title">Cuentas creadas</span>
            <div className="card-value">{metricas.totalCuentas}</div>
            <small style={{ color: 'var(--text-muted)', marginTop: 8 }}>Registradas en PostgreSQL</small>
          </div>

          <div className="card">
            <span className="card-title">Transferencias ejecutadas</span>
            <div className="card-value">{metricas.totalTransferencias}</div>
            <small style={{ color: 'var(--text-muted)', marginTop: 8 }}>Con consistencia e idempotencia</small>
          </div>

          <div className="card">
            <span className="card-title">Usuarios registrados</span>
            <div className="card-value">{metricas.totalUsuarios}</div>
            <small style={{ color: 'var(--text-muted)', marginTop: 8 }}>Clientes y administradores</small>
          </div>
        </div>
      )}

      <h2>Registro de Auditoría Transaccional (Top 100)</h2>
      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Operación</th>
              <th>Detalle</th>
              <th>Correlation ID</th>
              <th>Fecha UTC</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {auditorias.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ textAlign: 'center', color: 'var(--text-dim)', padding: 24 }}>
                  No hay registros de auditoría aún.
                </td>
              </tr>
            ) : (
              auditorias.map((a) => (
                <tr key={a.id}>
                  <td>#{a.id}</td>
                  <td>
                    <strong>{a.operacion}</strong>
                  </td>
                  <td>{a.detalle || '—'}</td>
                  <td>
                    <code style={{ fontSize: '0.78rem', background: 'var(--bg-primary)', padding: '2px 6px', borderRadius: 4 }}>
                      {a.correlationId ? a.correlationId.slice(0, 13) + '...' : '—'}
                    </code>
                  </td>
                  <td>{new Date(a.fecha).toLocaleString('es-AR')}</td>
                  <td>
                    <span className={`badge ${a.exito ? 'badge-in' : 'badge-out'}`}>
                      {a.exito ? 'ÉXITO' : 'FALLO'}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
