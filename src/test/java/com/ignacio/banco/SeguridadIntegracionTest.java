package com.ignacio.banco;

import com.ignacio.banco.cuentas.Cuenta;
import com.ignacio.banco.cuentas.CuentaRepository;

import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties="app.mail.enabled=true")
@ActiveProfiles("test")
class SeguridadIntegracionTest {
    @LocalServerPort int puerto;
    @org.springframework.beans.factory.annotation.Autowired CuentaRepository repositorio;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    org.springframework.mail.javamail.JavaMailSender correo;

    private HttpClient cliente() {
        return HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
    }
    private HttpResponse<String> get(HttpClient c,String ruta) throws Exception {
        return c.send(HttpRequest.newBuilder(URI.create("http://localhost:"+puerto+ruta))
            .header("Accept","application/json").GET().build(),HttpResponse.BodyHandlers.ofString());
    }
    private String token(HttpClient c) throws Exception {
        String json=get(c,"/api/auth/csrf").body();
        var match=Pattern.compile("\"token\"\\s*:\"([^\"]+)\"").matcher(json);
        assertTrue(match.find(),json);return match.group(1);
    }
    private HttpResponse<String> enviar(HttpClient c,String ruta,String json) throws Exception {
        return c.send(HttpRequest.newBuilder(URI.create("http://localhost:"+puerto+ruta))
            .header("Content-Type","application/json").header("X-CSRF-TOKEN",token(c))
            .POST(HttpRequest.BodyPublishers.ofString(json)).build(),HttpResponse.BodyHandlers.ofString());
    }
    private void registrar(HttpClient c,String email) throws Exception {
        var r=enviar(c,"/api/auth/registro","{\"nombre\":\"Prueba\",\"email\":\""+email+"\",\"password\":\"PruebaBanco2026!\"}");
        assertEquals(201,r.statusCode(),r.body());assertFalse(r.body().contains("password"));
    }
    private void login(HttpClient c,String email,String password) throws Exception {
        String body="username="+URLEncoder.encode(email,StandardCharsets.UTF_8)+"&password="+
            URLEncoder.encode(password,StandardCharsets.UTF_8)+"&_csrf="+URLEncoder.encode(token(c),StandardCharsets.UTF_8);
        var r=c.send(HttpRequest.newBuilder(URI.create("http://localhost:"+puerto+"/login"))
            .header("Content-Type","application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(302,r.statusCode(),r.body());assertTrue(r.headers().firstValue("location").orElse("").endsWith("/index.html"));
    }
    private long id(String json) {
        var m=Pattern.compile("\"id\":([0-9]+)").matcher(json);assertTrue(m.find(),json);return Long.parseLong(m.group(1));
    }
    @Test void registroLoginPropiedadCsrfYCambioPassword() throws Exception {
        HttpClient a=cliente(),b=cliente();String prefijo=UUID.randomUUID().toString();
        String correoA=prefijo+"a@example.com",correoB=prefijo+"b@example.com";
        assertEquals(401,get(a,"/api/cuentas").statusCode());
        assertEquals(200,get(a,"/login.html").statusCode());
        var documentacion=get(a,"/v3/api-docs");
        assertEquals(200,documentacion.statusCode(),documentacion.body());
        assertTrue(documentacion.body().contains("/api/transferencias"));
        assertFalse(documentacion.body().contains("passwordHash"));
        registrar(a,correoA);registrar(b,correoB);login(a,correoA,"PruebaBanco2026!");login(b,correoB,"PruebaBanco2026!");
        var creada=enviar(a,"/api/cuentas","{\"numeroCuenta\":\"T-"+prefijo.substring(0,10)+"\"}");
        assertEquals(201,creada.statusCode(),creada.body());long cuenta=id(creada.body());
        assertEquals(200,get(a,"/api/cuentas/"+cuenta).statusCode());
        assertEquals("[]",get(b,"/api/cuentas").body());
        assertEquals(404,get(b,"/api/cuentas/"+cuenta).statusCode());
        assertEquals(404,get(b,"/api/cuentas/"+cuenta+"/movimientos").statusCode());
        assertEquals("[]",get(b,"/api/transferencias").body());

        var destino=enviar(b,"/api/cuentas","{\"numeroCuenta\":\"D-"+prefijo.substring(0,10)+"\"}");
        assertEquals(201,destino.statusCode(),destino.body());long destinoId=id(destino.body());
        Cuenta origen=repositorio.findById(cuenta).orElseThrow();
        origen.acreditar(new java.math.BigDecimal("1000.00"));repositorio.saveAndFlush(origen);
        String clave=UUID.randomUUID().toString();
        String transferencia="{\"origenId\":"+cuenta+",\"destinoId\":"+destinoId+
            ",\"importe\":\"100.00\",\"claveIdempotencia\":\""+clave+"\"}";
        assertEquals(404,enviar(b,"/api/transferencias",transferencia).statusCode());
        var operacion=enviar(a,"/api/transferencias",transferencia);
        assertEquals(200,operacion.statusCode(),operacion.body());
        var repetida=enviar(a,"/api/transferencias",transferencia);
        assertEquals(id(operacion.body()),id(repetida.body()));
        assertEquals(409,enviar(a,"/api/transferencias",transferencia.replace("100.00","200.00")).statusCode());
        assertEquals(0,new java.math.BigDecimal("900.00").compareTo(repositorio.findById(cuenta).orElseThrow().getSaldo()));
        // Fallo al acreditar: comprueba rollback real del débito ya ejecutado.
        Cuenta receptora=repositorio.findById(destinoId).orElseThrow();
        receptora.acreditar(new java.math.BigDecimal("99999999999999899.99"));repositorio.saveAndFlush(receptora);
        String fallida=transferencia.replace(clave,UUID.randomUUID().toString());
        assertEquals(400,enviar(a,"/api/transferencias",fallida).statusCode());
        assertEquals(0,new java.math.BigDecimal("900.00").compareTo(repositorio.findById(cuenta).orElseThrow().getSaldo()));
        var sinCsrf=b.send(HttpRequest.newBuilder(URI.create("http://localhost:"+puerto+"/api/cuentas"))
            .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString("{}"))
            .build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(403,sinCsrf.statusCode());
        assertEquals(400,enviar(a,"/api/perfil/password","{\"passwordActual\":\"incorrecta\",\"passwordNueva\":\"NuevaBanco2026!\"}").statusCode());
        assertEquals(200,enviar(a,"/api/perfil/password","{\"passwordActual\":\"PruebaBanco2026!\",\"passwordNueva\":\"NuevaBanco2026!\"}").statusCode());
        assertEquals(401,get(a,"/api/cuentas").statusCode());
        login(a,correoA,"NuevaBanco2026!");assertEquals(200,get(a,"/api/perfil").statusCode());
        var recuperacion=enviar(a,"/api/auth/recuperar","{\"email\":\""+correoB+"\"}");
        assertEquals(200,recuperacion.statusCode(),recuperacion.body());
        var captor=org.mockito.ArgumentCaptor.forClass(org.springframework.mail.SimpleMailMessage.class);
        org.mockito.Mockito.verify(correo).send(captor.capture());
        String mensaje=captor.getValue().getText();
        var enlace=Pattern.compile("#token=([A-Za-z0-9_-]+)").matcher(mensaje);
        assertTrue(enlace.find());String secreto=enlace.group(1);
        String cambio="{\"token\":\""+secreto+"\",\"password\":\"Recuperada2026!\"}";
        assertEquals(200,enviar(a,"/api/auth/restablecer",cambio).statusCode());
        assertEquals(400,enviar(a,"/api/auth/restablecer",cambio).statusCode());
        var expirada=get(b,"/api/cuentas");assertTrue(expirada.statusCode()==401||expirada.statusCode()==302);
        login(b,correoB,"Recuperada2026!");assertEquals(200,get(b,"/api/perfil").statusCode());

    }
}
