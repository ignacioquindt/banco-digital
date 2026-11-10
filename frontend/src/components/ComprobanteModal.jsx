import { useState } from 'react';

export default function ComprobanteModal({ transferencia, onClose }) {
  const [copiado, setCopiado] = useState(false);

  if (!transferencia) return null;

  const fechaFormateada = transferencia.fecha
    ? new Date(transferencia.fecha).toLocaleString('es-AR', {
        dateStyle: 'full',
        timeStyle: 'medium',
      })
    : new Date().toLocaleString('es-AR');

  const handlePrint = () => {
    window.print();
  };

  const handleCopiar = () => {
    const texto = `COMPROBANTE DE TRANSFERENCIA - BANCO DIGITAL
Operación: #${transferencia.id || 'N/A'}
Fecha: ${fechaFormateada}
Importe: $ ${Number(transferencia.importe).toLocaleString('es-AR', { minimumFractionDigits: 2 })} ARS
Cuenta Origen: Cuenta #${transferencia.origenId}
Cuenta Destino: Cuenta #${transferencia.destinoId}
Clave de Idempotencia: ${transferencia.claveIdempotencia || 'N/A'}
Estado: Transferencia Acreditada (Exitosa)`;

    navigator.clipboard.writeText(texto);
    setCopiado(true);
    setTimeout(() => setCopiado(false), 2500);
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content" style={{ maxWidth: 520, padding: 0, overflow: 'hidden' }}>
        {/* Cabecera del Comprobante */}
        <div
          style={{
            background: 'linear-gradient(135deg, #0284c7 0%, #0369a1 100%)',
            color: '#fff',
            padding: '24px 24px 20px 24px',
            textAlign: 'center',
            position: 'relative',
          }}
        >
          <button
            onClick={onClose}
            style={{
              position: 'absolute',
              top: 14,
              right: 14,
              background: 'rgba(0,0,0,0.2)',
              border: 'none',
              color: '#fff',
              fontSize: '1.1rem',
              borderRadius: '50%',
              width: 32,
              height: 32,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            ✕
          </button>

          <div style={{ fontSize: '1.7rem', marginBottom: 4 }}>⬡</div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0 }}>BANCO DIGITAL</h2>
          <p style={{ fontSize: '0.82rem', opacity: 0.9, marginTop: 2 }}>Comprobante Oficial de Transferencia</p>

          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: 6,
              background: 'rgba(16, 185, 129, 0.25)',
              border: '1px solid rgba(16, 185, 129, 0.5)',
              color: '#a7f3d0',
              padding: '4px 12px',
              borderRadius: 20,
              fontSize: '0.8rem',
              fontWeight: 600,
              marginTop: 12,
            }}
          >
            <span>✓</span> ACREDITADA EXITOSAMENTE
          </div>
        </div>

        {/* Cuerpo del ticket */}
        <div style={{ padding: '24px' }}>
          {/* Importe Grande */}
          <div style={{ textAlign: 'center', marginBottom: 24, paddingBottom: 16, borderBottom: '1px dashed var(--border)' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Monto transferido</span>
            <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--success)', marginTop: 4 }}>
              $ {Number(transferencia.importe).toLocaleString('es-AR', { minimumFractionDigits: 2 })} <span style={{ fontSize: '1rem', fontWeight: 600 }}>ARS</span>
            </div>
          </div>

          {/* Detalles de la operación */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12, fontSize: '0.9rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>N° de Operación</span>
              <span style={{ fontWeight: 600 }}>#{transferencia.id || 'N/A'}</span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Fecha y Hora</span>
              <span style={{ fontWeight: 500 }}>{fechaFormateada}</span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Cuenta de Origen</span>
              <span style={{ fontWeight: 600, color: '#38bdf8' }}>Cuenta #{transferencia.origenId}</span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Cuenta de Destino</span>
              <span style={{ fontWeight: 600, color: '#38bdf8' }}>Cuenta #{transferencia.destinoId}</span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ color: 'var(--text-muted)' }}>Tipo de Operación</span>
              <span style={{ fontWeight: 500 }}>Transferencia Inmediata</span>
            </div>

            {transferencia.claveIdempotencia && (
              <div style={{ marginTop: 8, padding: 10, background: 'var(--bg-primary)', borderRadius: 6, border: '1px solid var(--border)' }}>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginBottom: 2 }}>
                  Clave de Idempotencia (UUID)
                </span>
                <code style={{ fontSize: '0.76rem', color: '#94a3b8', wordBreak: 'break-all' }}>
                  {transferencia.claveIdempotencia}
                </code>
              </div>
            )}
          </div>

          {/* Botones de acción */}
          <div style={{ display: 'flex', gap: 10, marginTop: 24 }}>
            <button
              onClick={handlePrint}
              className="btn btn-primary"
              style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6 }}
            >
              🖨️ Imprimir / Guardar PDF
            </button>
            <button
              onClick={handleCopiar}
              className="btn btn-secondary"
              style={{ padding: '8px 14px' }}
              title="Copiar texto del comprobante"
            >
              {copiado ? '✓ Copiado' : '📋 Copiar'}
            </button>
            <button onClick={onClose} className="btn btn-secondary">
              Cerrar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
