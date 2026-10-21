package com.ignacio.banco;

import com.ignacio.banco.cuentas.*;
import com.ignacio.banco.transferencias.*;
import com.ignacio.banco.usuarios.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

/** Base efímera exclusiva para pruebas. Nunca usar la base de desarrollo. */
@SpringBootTest
@ActiveProfiles("pgtest")
@EnabledIfEnvironmentVariable(named="TEST_DB_PASSWORD", matches=".+")
class TransferenciasPostgresIT {
    @Autowired CuentaRepository cuentas;
    @Autowired UsuarioRepository usuarios;
    @Autowired TransferenciaRepository transferencias;
    @Autowired TransferenciaService servicio;
    @Autowired JdbcTemplate jdbc;
    Cuenta origen, destino;
    Long usuarioId;

    @BeforeEach
    void preparar() {
        String sufijo=UUID.randomUUID().toString().substring(0,12);
        usuarioId=usuarios.saveAndFlush(new Usuario("Prueba",sufijo+"@example.com","solo-fixture-sin-login")).getId();
        origen=cuentas.saveAndFlush(new Cuenta("O-"+sufijo,usuarioId));
        destino=cuentas.saveAndFlush(new Cuenta("D-"+sufijo,usuarioId));
        origen.acreditar(new BigDecimal("100.00"));
        origen=cuentas.saveAndFlush(origen);
    }
    private Transferencia enviar(String importe, UUID clave) {
        return servicio.transferir(origen.getId(),destino.getId(),new BigDecimal(importe),clave,usuarioId);
    }
    private void saldo(Cuenta cuenta,String esperado) {
        assertEquals(0,new BigDecimal(esperado).compareTo(cuentas.findById(cuenta.getId()).orElseThrow().getSaldo()));
    }
    private List<Integer> simultaneas(UUID primera,UUID segunda,String importe) throws Exception {
        ExecutorService executor=Executors.newFixedThreadPool(2);
        CountDownLatch listas=new CountDownLatch(2),inicio=new CountDownLatch(1);
        java.util.function.Function<UUID,Callable<Integer>> tarea=clave -> () -> {
            listas.countDown();assertTrue(inicio.await(10,TimeUnit.SECONDS));
            try { enviar(importe,clave);return 200; }
            catch(ResponseStatusException ex) { return ex.getStatusCode().value(); }
        };
        try {
            Future<Integer> a=executor.submit(tarea.apply(primera));
            Future<Integer> b=executor.submit(tarea.apply(segunda));
            assertTrue(listas.await(10,TimeUnit.SECONDS));inicio.countDown();
            return List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS));
        } finally { inicio.countDown();executor.shutdownNow(); }
    }
    @Test void migracionesCreanEsquemaYRestricciones() {
        assertTrue(jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE success",Integer.class)>=1);
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->
            jdbc.update("UPDATE cuentas SET saldo=-1 WHERE id=?",origen.getId()));
        saldo(origen,"100.00");
    }
    @Test void dosReintentosSimultaneosDebitanUnaSolaVez() throws Exception {
        UUID clave=UUID.randomUUID();
        assertEquals(List.of(200,200),simultaneas(clave,clave,"1.00"));
        saldo(origen,"99.00");saldo(destino,"1.00");
        assertEquals(1,transferencias.buscarPorCuenta(origen.getId()).size());
        assertEquals(transferencias.findByClaveIdempotencia(clave).orElseThrow().getId(),enviar("1.00",clave).getId());
    }
    @Test void operacionesSimultaneasNoGastanMasQueElSaldo() throws Exception {
        List<Integer> resultados=new ArrayList<>(simultaneas(UUID.randomUUID(),UUID.randomUUID(),"75.00"));
        Collections.sort(resultados);assertEquals(List.of(200,400),resultados);
        saldo(origen,"25.00");saldo(destino,"75.00");
        assertEquals(1,transferencias.buscarPorCuenta(origen.getId()).size());
    }
    @Test void errorEnDestinoRevierteElDebito() {
        destino.acreditar(new BigDecimal("99999999999999999.99"));cuentas.saveAndFlush(destino);
        assertEquals(400,assertThrows(ResponseStatusException.class,()->enviar("1.00",UUID.randomUUID())).getStatusCode().value());
        saldo(origen,"100.00");assertTrue(transferencias.buscarPorCuenta(origen.getId()).isEmpty());
    }
    @Test void rechazaOperarSobreUnaCuentaAjena() {
        assertEquals(404,assertThrows(ResponseStatusException.class,()->servicio.transferir(
            origen.getId(),destino.getId(),BigDecimal.ONE,UUID.randomUUID(),usuarioId+100000L)).getStatusCode().value());
        saldo(origen,"100.00");saldo(destino,"0.00");
    }
    @Test void mismaClaveConOtrosDatosNoModificaSaldos() {
        UUID clave=UUID.randomUUID();enviar("10.00",clave);
        assertEquals(409,assertThrows(ResponseStatusException.class,()->enviar("20.00",clave)).getStatusCode().value());
        saldo(origen,"90.00");saldo(destino,"10.00");
    }

    @Test void adoptaElEsquemaAnteriorSinPerderDatos() throws Exception {
        String esquema="legado_"+UUID.randomUUID().toString().replace("-", "");
        jdbc.execute("CREATE SCHEMA "+esquema);
        try {
            String sql;
            try(var in=getClass().getResourceAsStream("/db/migration/V1__esquema_inicial.sql")) {
                sql=new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
            }
            String url=System.getenv().getOrDefault("TEST_DB_URL","jdbc:postgresql://localhost:5433/banco_test");
            String usuario=System.getenv().getOrDefault("TEST_DB_USERNAME","banco_test");
            String password=System.getenv("TEST_DB_PASSWORD");
            // Conexiones propias: el cambio de search_path no debe filtrarse al pool de la app.
            try(var conexion=java.sql.DriverManager.getConnection(url,usuario,password)) {
                conexion.setSchema(esquema);
                try(var statement=conexion.createStatement()) {
                    statement.execute(sql);
                    statement.execute("INSERT INTO usuarios(nombre,email,password_hash) VALUES ('Legado','legado@example.com','fixture')");
                }
            }
            org.flywaydb.core.Flyway.configure().dataSource(url,usuario,password)
                .schemas(esquema).defaultSchema(esquema)
                .baselineOnMigrate(true).baselineVersion("0").load().migrate();
            assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM "+esquema+".usuarios WHERE email='legado@example.com'",Integer.class));
            assertEquals(2,jdbc.queryForObject("SELECT count(*) FROM "+esquema+".flyway_schema_history WHERE success",Integer.class));
        } finally { jdbc.execute("DROP SCHEMA "+esquema+" CASCADE"); }
    }
}
