package com.ignacio.banco.seguridad;



import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import org.springframework.web.filter.OncePerRequestFilter;

// Límite local por IP para login, registro y recuperación. Para varias instancias usar Redis/gateway.
public class LimiteSolicitudesFilter extends OncePerRequestFilter {
    private final Map<String, Ventana> ventanas = new HashMap<>();
    private record Ventana(long inicio, int cantidad) {}
    private synchronized boolean permitir(String ip) {
        long ahora=System.currentTimeMillis();
        ventanas.entrySet().removeIf(e -> ahora-e.getValue().inicio()>60000);
        Ventana v=ventanas.get(ip);
        if(v==null){if(ventanas.size()>=10000)return false;ventanas.put(ip,new Ventana(ahora,1));return true;}
        if(v.cantidad()>=20)return false;
        ventanas.put(ip,new Ventana(v.inicio(),v.cantidad()+1));return true;
    }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,
            FilterChain chain) throws ServletException,IOException {
        String ruta=request.getServletPath();
        boolean sensible="POST".equals(request.getMethod()) &&
            (ruta.equals("/login") || ruta.startsWith("/api/auth/") || ruta.equals("/api/perfil/password"));
        if(sensible && !permitir(request.getRemoteAddr())) {
            response.setStatus(429);response.setHeader("Retry-After","60");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":429,\"mensaje\":\"Demasiados intentos. Esperá un minuto.\"}");return;
        }
        chain.doFilter(request,response);
    }
}
