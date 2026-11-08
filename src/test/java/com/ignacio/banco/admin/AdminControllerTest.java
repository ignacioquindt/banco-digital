package com.ignacio.banco.admin;

import com.ignacio.banco.auditoria.AuditoriaOperacion;
import com.ignacio.banco.auditoria.AuditoriaRepository;
import com.ignacio.banco.cuentas.CuentaRepository;
import com.ignacio.banco.transferencias.TransferenciaRepository;
import com.ignacio.banco.usuarios.Rol;
import com.ignacio.banco.usuarios.Usuario;
import com.ignacio.banco.usuarios.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    private UsuarioRepository usuarioRepository;
    private AuditoriaRepository auditoriaRepository;
    private CuentaRepository cuentaRepository;
    private TransferenciaRepository transferenciaRepository;
    private AdminController controller;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        auditoriaRepository = mock(AuditoriaRepository.class);
        cuentaRepository = mock(CuentaRepository.class);
        transferenciaRepository = mock(TransferenciaRepository.class);

        controller = new AdminController(
                usuarioRepository,
                auditoriaRepository,
                cuentaRepository,
                transferenciaRepository
        );
    }

    @Test
    void listarUsuariosRetornaListaDeDto() {
        Usuario usuario = new Usuario("Admin", "admin@banco.local", "hash", Rol.ROLE_ADMIN);
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<AdminUsuarioResponse> respuesta = controller.listarUsuarios();

        assertEquals(1, respuesta.size());
        assertEquals("Admin", respuesta.get(0).nombre());
        assertEquals(Rol.ROLE_ADMIN, respuesta.get(0).rol());
        verify(usuarioRepository).findAll();
    }

    @Test
    void cambiarRolActualizaElUsuario() {
        Usuario usuario = new Usuario("Cliente", "cli@banco.local", "hash", Rol.ROLE_CLIENTE);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        AdminUsuarioResponse res = controller.cambiarRol(10L, new CambiarRolRequest(Rol.ROLE_AUDITOR));

        assertEquals(Rol.ROLE_AUDITOR, res.rol());
        verify(usuarioRepository).saveAndFlush(usuario);
    }

    @Test
    void cambiarEstadoDesactivaUsuario() {
        Usuario usuario = new Usuario("Cliente", "cli@banco.local", "hash", Rol.ROLE_CLIENTE);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        AdminUsuarioResponse res = controller.cambiarEstado(10L, new CambiarEstadoRequest(false));

        assertFalse(res.activo());
        verify(usuarioRepository).saveAndFlush(usuario);
    }

    @Test
    void obtenerMetricasCalculaTotales() {
        when(usuarioRepository.count()).thenReturn(15L);
        when(cuentaRepository.count()).thenReturn(30L);
        when(cuentaRepository.calcularSaldoTotal()).thenReturn(new BigDecimal("150000.00"));
        when(transferenciaRepository.count()).thenReturn(45L);

        MetricasSistemaResponse metricas = controller.obtenerMetricas();

        assertEquals(15L, metricas.totalUsuarios());
        assertEquals(30L, metricas.totalCuentas());
        assertEquals(0, new BigDecimal("150000.00").compareTo(metricas.saldoTotalEnCirculacion()));
        assertEquals(45L, metricas.totalTransferencias());
    }

    @Test
    void listarAuditoriaRetornaRegistrosMapeados() {
        AuditoriaOperacion op = new AuditoriaOperacion(
                1L, "admin@banco.local", "TRANSFERENCIA", "Detalle", "127.0.0.1", "corr-123", true
        );
        when(auditoriaRepository.findTop100ByOrderByFechaDesc()).thenReturn(List.of(op));

        var respuesta = controller.listarAuditoria();

        assertEquals(1, respuesta.size());
        assertEquals("TRANSFERENCIA", respuesta.get(0).operacion());
        assertEquals("corr-123", respuesta.get(0).correlationId());
        assertTrue(respuesta.get(0).exito());
        verify(auditoriaRepository).findTop100ByOrderByFechaDesc();
    }
}
