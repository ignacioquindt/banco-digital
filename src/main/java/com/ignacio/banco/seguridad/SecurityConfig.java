package com.ignacio.banco.seguridad;



import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.session.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityConfig {
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public SessionRegistry sessionRegistry() { return new SessionRegistryImpl(); }
    @Bean public HttpSessionEventPublisher httpSessionEventPublisher() { return new HttpSessionEventPublisher(); }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SessionRegistry registry) throws Exception {
        http.addFilterBefore(new LimiteSolicitudesFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/login", "/login.html", "/registro.html",
                "/recuperar.html", "/restablecer.html", "/css/**", "/js/**",
                "/api/estado", "/api/auth/csrf", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/registro",
                "/api/auth/recuperar", "/api/auth/restablecer").permitAll()
            .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
            .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index.html", true)
                .failureUrl("/login.html?error=1").permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login.html?salida=1")
                .invalidateHttpSession(true).deleteCookies("JSESSIONID"))
            .sessionManagement(session -> session.maximumSessions(-1)
                .sessionRegistry(registry).expiredUrl("/login.html?expirada=1"))
            .exceptionHandling(errors -> errors.defaultAuthenticationEntryPointFor(
                (request, response, ex) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":401,\"mensaje\":\"Debés iniciar sesión\"}");
                }, request -> request.getServletPath().startsWith("/api/")));
        return http.build();
    }
}
