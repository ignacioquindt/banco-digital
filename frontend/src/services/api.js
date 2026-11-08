let csrfCache = null;

export async function getCsrfToken() {
  if (csrfCache) return csrfCache;
  const res = await fetch('/api/auth/csrf');
  if (!res.ok) throw new Error('No se pudo obtener el token CSRF');
  const data = await res.json();
  csrfCache = { header: data.headerName, token: data.token };
  return csrfCache;
}

export function clearCsrfToken() {
  csrfCache = null;
}

export async function request(path, options = {}) {
  const headers = {
    Accept: 'application/json',
    ...(options.headers || {}),
  };

  const method = (options.method || 'GET').toUpperCase();

  if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
    const csrf = await getCsrfToken();
    headers[csrf.header] = csrf.token;
    if (options.body && typeof options.body === 'object') {
      headers['Content-Type'] = 'application/json';
      options.body = JSON.stringify(options.body);
    }
  }

  const res = await fetch(path, { ...options, headers });

  if (res.status === 401) {
    throw new Error('UNAUTHORIZED');
  }

  if (!res.ok) {
    let mensaje = `Error HTTP ${res.status}`;
    try {
      const errorJson = await res.json();
      mensaje = errorJson.message || errorJson.mensaje || errorJson.detail || mensaje;
    } catch {
      // Ignore parse failure
    }
    throw new Error(mensaje);
  }

  const contentType = res.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return res.json();
  }
  return res.text();
}

export const BancoApi = {
  // Autenticación
  login: async (username, password) => {
    const csrf = await getCsrfToken();
    const params = new URLSearchParams();
    params.append('username', username);
    params.append('password', password);
    params.append('_csrf', csrf.token);

    const res = await fetch('/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: params.toString(),
      redirect: 'manual',
    });

    if (res.status === 401 || res.type === 'opaqueredirect' || res.status === 302) {
      clearCsrfToken();
      // Verificar si logueó con éxito
      const perfil = await fetch('/api/perfil');
      if (perfil.ok) return perfil.json();
      throw new Error('Credenciales inválidas');
    }

    const perfil = await fetch('/api/perfil');
    if (!perfil.ok) throw new Error('Credenciales inválidas');
    return perfil.json();
  },

  logout: async () => {
    const csrf = await getCsrfToken();
    await fetch('/logout', {
      method: 'POST',
      headers: { [csrf.header]: csrf.token },
    });
    clearCsrfToken();
  },

  getPerfil: () => request('/api/perfil'),

  // Cuentas
  getCuentas: () => request('/api/cuentas'),

  crearCuenta: (numeroCuenta) =>
    request('/api/cuentas', {
      method: 'POST',
      body: { numeroCuenta },
    }),

  depositar: (cuentaId, importe, claveIdempotencia) =>
    request(`/api/cuentas/${cuentaId}/depositar`, {
      method: 'POST',
      body: { importe, claveIdempotencia },
    }),

  extraer: (cuentaId, importe, claveIdempotencia) =>
    request(`/api/cuentas/${cuentaId}/extraer`, {
      method: 'POST',
      body: { importe, claveIdempotencia },
    }),

  getMovimientosCuenta: (cuentaId) =>
    request(`/api/cuentas/${cuentaId}/movimientos`),

  getMovimientosPaginados: (cuentaId, page = 0, size = 10) =>
    request(`/api/cuentas/${cuentaId}/movimientos/paginado?page=${page}&size=${size}&sort=fecha,desc`),

  // Transferencias
  getTransferencias: () => request('/api/transferencias'),

  transferir: (origenId, destinoId, importe, claveIdempotencia) =>
    request('/api/transferencias', {
      method: 'POST',
      body: { origenId, destinoId, importe, claveIdempotencia },
    }),

  // Admin
  getMetricasAdmin: () => request('/api/admin/metricas'),
  getAuditoriaAdmin: () => request('/api/admin/auditoria'),
  getUsuariosAdmin: () => request('/api/admin/usuarios'),
};
