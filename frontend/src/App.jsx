import { useState, useEffect } from 'react';
import { BancoApi } from './services/api';
import LoginView from './components/LoginView';
import CashInModal from './components/CashInModal';
import CashOutModal from './components/CashOutModal';
import TransferModal from './components/TransferModal';
import CreateAccountModal from './components/CreateAccountModal';
import AdminPanel from './components/AdminPanel';

export default function App() {
  const [perfil, setPerfil] = useState(null);
  const [cuentas, setCuentas] = useState([]);
  const [transferencias, setTransferencias] = useState([]);
  const [currentView, setCurrentView] = useState('inicio');
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState(null);

  // Modales
  const [modalCashIn, setModalCashIn] = useState(null);
  const [modalCashOut, setModalCashOut] = useState(null);
  const [modalTransfer, setModalTransfer] = useState(false);
  const [modalCreateAccount, setModalCreateAccount] = useState(false);

  const cargarDatos = async () => {
    try {
      const [p, c, t] = await Promise.all([
        BancoApi.getPerfil(),
        BancoApi.getCuentas(),
        BancoApi.getTransferencias(),
      ]);
      setPerfil(p);
      setCuentas(c);
      setTransferencias(t);
    } catch (err) {
      if (err.message === 'UNAUTHORIZED') {
        setPerfil(null);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, []);

  const handleLogout = async () => {
    try {
      await BancoApi.logout();
      setPerfil(null);
      setCuentas([]);
      setTransferencias([]);
    } catch {
      setPerfil(null);
    }
  };

  const showSuccessNotice = (msg) => {
    setNotice({ type: 'success', message: msg });
    cargarDatos();
    setTimeout(() => setNotice(null), 5000);
  };

  if (loading) {
    return (
      <div className="login-wrapper">
        <div style={{ color: 'var(--text-muted)' }}>Cargando Banco Digital...</div>
      </div>
    );
  }

  if (!perfil) {
    return (
      <LoginView
        onLoginSuccess={(nuevoPerfil) => {
          setPerfil(nuevoPerfil);
          cargarDatos();
        }}
      />
    );
  }

  const saldoTotal = cuentas.reduce((acc, c) => acc + Number(c.saldo), 0);

  return (
    <div className="app-container">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="brand">
          Banco<span>Digital</span>
        </div>
        <div className="brand-badge">Simulador Bancario · React SPA</div>

        <nav className="nav-menu">
          <button
            className={`nav-item ${currentView === 'inicio' ? 'active' : ''}`}
            onClick={() => setCurrentView('inicio')}
          >
            📊 Resumen
          </button>
          <button
            className={`nav-item ${currentView === 'cuentas' ? 'active' : ''}`}
            onClick={() => setCurrentView('cuentas')}
          >
            💳 Mis cuentas ({cuentas.length})
          </button>
          <button
            className={`nav-item ${currentView === 'transferencias' ? 'active' : ''}`}
            onClick={() => setCurrentView('transferencias')}
          >
            🔄 Transferencias
          </button>
          <button
            className={`nav-item ${currentView === 'admin' ? 'active' : ''}`}
            onClick={() => setCurrentView('admin')}
          >
            🛡️ Administración
          </button>
        </nav>

        <div style={{ padding: '16px 0', borderTop: '1px solid var(--border)', fontSize: '0.78rem', color: 'var(--text-dim)' }}>
          Backend: Java 21 · Spring Boot<br />
          Patrón: Transactional Outbox<br />
          Persistencia: PostgreSQL
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <header className="topbar">
          <div className="user-badge">
            <span style={{ fontWeight: 600 }}>Hola, {perfil.nombre}</span>
            <span className="badge badge-client">ID: {perfil.id}</span>
          </div>
          <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
            <button className="btn btn-primary" onClick={() => setModalTransfer(true)} disabled={cuentas.length === 0}>
              + Nueva transferencia
            </button>
            <button className="btn btn-secondary" onClick={handleLogout}>
              Cerrar sesión
            </button>
          </div>
        </header>

        <div className="page-body">
          {notice && <div className={`alert alert-${notice.type}`}>{notice.message}</div>}

          {/* VISTA: INICIO */}
          {currentView === 'inicio' && (
            <div>
              <h1>Tu resumen financiero</h1>
              <p className="subtitle">Visualización consolidada de saldos y operaciones en tiempo real.</p>

              <div className="grid-cards">
                <div className="card">
                  <span className="card-title">Saldo total en ARS</span>
                  <div className="card-value" style={{ color: 'var(--success)' }}>
                    $ {saldoTotal.toLocaleString('es-AR', { minimumFractionDigits: 2 })}
                  </div>
                  <small style={{ color: 'var(--text-muted)', marginTop: 8 }}>Disponible entre tus cuentas</small>
                </div>

                <div className="card">
                  <span className="card-title">Cuentas bancarias activas</span>
                  <div className="card-value">{cuentas.length}</div>
                  <div className="card-actions">
                    <button className="btn btn-secondary" onClick={() => setCurrentView('cuentas')}>
                      Ver mis cuentas
                    </button>
                  </div>
                </div>

                <div className="card">
                  <span className="card-title">Transferencias registradas</span>
                  <div className="card-value">{transferencias.length}</div>
                  <div className="card-actions">
                    <button className="btn btn-secondary" onClick={() => setCurrentView('transferencias')}>
                      Ver historial
                    </button>
                  </div>
                </div>
              </div>

              <h2>Últimos movimientos</h2>
              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>Fecha</th>
                      <th>Origen</th>
                      <th>Destino</th>
                      <th>Importe (ARS)</th>
                      <th>Referencia</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transferencias.length === 0 ? (
                      <tr>
                        <td colSpan="5" style={{ textAlign: 'center', color: 'var(--text-dim)', padding: 24 }}>
                          Todavía no registraste transferencias.
                        </td>
                      </tr>
                    ) : (
                      transferencias.slice(0, 5).map((t) => (
                        <tr key={t.id}>
                          <td>{new Date(t.fecha).toLocaleString('es-AR')}</td>
                          <td>Cuenta #{t.origenId}</td>
                          <td>Cuenta #{t.destinoId}</td>
                          <td style={{ fontWeight: 600 }}>$ {Number(t.importe).toLocaleString('es-AR')}</td>
                          <td>
                            <code style={{ fontSize: '0.78rem', background: 'var(--bg-primary)', padding: '2px 6px', borderRadius: 4 }}>
                              {t.claveIdempotencia ? t.claveIdempotencia.slice(0, 8) + '...' : '—'}
                            </code>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* VISTA: CUENTAS */}
          {currentView === 'cuentas' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                <div>
                  <h1>Mis cuentas bancarias</h1>
                  <p className="subtitle">Administrá tus saldos, realizá depósitos (Cash-in) y extracciones.</p>
                </div>
                <button className="btn btn-primary" onClick={() => setModalCreateAccount(true)}>
                  + Abrir nueva cuenta
                </button>
              </div>

              <div className="grid-cards">
                {cuentas.map((c) => (
                  <div key={c.id} className="card">
                    <span className="card-title">CUENTA EN {c.moneda} · ID: #{c.id}</span>
                    <h3 style={{ margin: '4px 0 12px 0' }}>{c.numeroCuenta}</h3>
                    <div className="card-value" style={{ color: 'var(--success)', marginBottom: 16 }}>
                      $ {Number(c.saldo).toLocaleString('es-AR', { minimumFractionDigits: 2 })}
                    </div>

                    <div className="card-actions">
                      <button className="btn btn-success" onClick={() => setModalCashIn(c)}>
                        Cargar saldo
                      </button>
                      <button className="btn btn-secondary" onClick={() => setModalCashOut(c)}>
                        Extraer
                      </button>
                      <button className="btn btn-outline" onClick={() => setModalTransfer(c.id)}>
                        Transferir
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* VISTA: TRANSFERENCIAS */}
          {currentView === 'transferencias' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                <div>
                  <h1>Historial de Transferencias</h1>
                  <p className="subtitle">Operaciones registradas con consistencia transaccional y bloqueo anti-deadlock.</p>
                </div>
                <button className="btn btn-primary" onClick={() => setModalTransfer(true)} disabled={cuentas.length === 0}>
                  + Nueva transferencia
                </button>
              </div>

              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Fecha</th>
                      <th>Origen</th>
                      <th>Destino</th>
                      <th>Importe (ARS)</th>
                      <th>Clave de Idempotencia</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transferencias.length === 0 ? (
                      <tr>
                        <td colSpan="6" style={{ textAlign: 'center', color: 'var(--text-dim)', padding: 24 }}>
                          No hay movimientos registrados.
                        </td>
                      </tr>
                    ) : (
                      transferencias.map((t) => (
                        <tr key={t.id}>
                          <td>#{t.id}</td>
                          <td>{new Date(t.fecha).toLocaleString('es-AR')}</td>
                          <td>Cuenta #{t.origenId}</td>
                          <td>Cuenta #{t.destinoId}</td>
                          <td style={{ fontWeight: 600 }}>$ {Number(t.importe).toLocaleString('es-AR')}</td>
                          <td>
                            <code style={{ fontSize: '0.78rem', background: 'var(--bg-primary)', padding: '2px 6px', borderRadius: 4 }}>
                              {t.claveIdempotencia}
                            </code>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* VISTA: ADMIN */}
          {currentView === 'admin' && <AdminPanel />}
        </div>
      </main>

      {/* Modales */}
      {modalCashIn && (
        <CashInModal
          cuenta={modalCashIn}
          onClose={() => setModalCashIn(null)}
          onSuccess={showSuccessNotice}
        />
      )}

      {modalCashOut && (
        <CashOutModal
          cuenta={modalCashOut}
          onClose={() => setModalCashOut(null)}
          onSuccess={showSuccessNotice}
        />
      )}

      {modalTransfer && (
        <TransferModal
          cuentas={cuentas}
          defaultOrigenId={typeof modalTransfer === 'number' ? modalTransfer : null}
          onClose={() => setModalTransfer(false)}
          onSuccess={showSuccessNotice}
        />
      )}

      {modalCreateAccount && (
        <CreateAccountModal
          onClose={() => setModalCreateAccount(false)}
          onSuccess={showSuccessNotice}
        />
      )}
    </div>
  );
}
