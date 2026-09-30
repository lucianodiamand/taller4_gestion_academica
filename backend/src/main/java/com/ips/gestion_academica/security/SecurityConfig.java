package com.ips.gestion_academica.security;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ips.gestion_academica.util.Json;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil) {
        return new JwtAuthFilter(jwtUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(Json.stringify(
                            Map.of("error", "Debe iniciar sesion para acceder a este recurso")));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(Json.stringify(
                            Map.of("error", "No tiene permisos para realizar esta accion")));
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()

                // Usuarios: recurso sensible, solo ADMIN puede listar/crear/modificar/dar de baja.
                // Excepcion: cualquier usuario logueado puede ver/editar SU PROPIO perfil
                // y cambiar SU PROPIA contrasena (nunca la de otro usuario).
                .requestMatchers(HttpMethod.GET, "/api/usuarios/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/me/password").authenticated()
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                // Materias: todos los logueados pueden consultar, el ABM es solo de ADMIN
                .requestMatchers(HttpMethod.GET, "/api/materias/**").authenticated()
                .requestMatchers("/api/materias/**").hasRole("ADMIN")

                // Cursos: todos los logueados pueden consultar, el ABM es solo de ADMIN
                .requestMatchers(HttpMethod.GET, "/api/cursos/**").authenticated()
                .requestMatchers("/api/cursos/**").hasRole("ADMIN")

                // Inscripciones: ALUMNO se inscribe a si mismo (validado en el service),
                // PROFESOR y ADMIN gestionan estado y consultan, solo ADMIN da de baja
                .requestMatchers(HttpMethod.POST, "/api/inscripciones").hasAnyRole("ADMIN", "ALUMNO")
                .requestMatchers(HttpMethod.PUT, "/api/inscripciones/*/estado").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.DELETE, "/api/inscripciones/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/inscripciones/mias").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/inscripciones/**").hasAnyRole("ADMIN", "PROFESOR")

                // Examenes: todos los logueados pueden consultar, la gestion es de ADMIN y PROFESOR
                .requestMatchers(HttpMethod.GET, "/api/examenes/**").authenticated()
                .requestMatchers("/api/examenes/**").hasAnyRole("ADMIN", "PROFESOR")

                // Inscripciones a examen (mesas)
                .requestMatchers(HttpMethod.POST, "/api/inscripciones-examen").hasAnyRole("ADMIN", "ALUMNO")
                .requestMatchers(HttpMethod.PUT, "/api/inscripciones-examen/*/nota").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.DELETE, "/api/inscripciones-examen/**").hasAnyRole("ADMIN", "ALUMNO")
                .requestMatchers(HttpMethod.GET, "/api/inscripciones-examen/**").hasAnyRole("ADMIN", "PROFESOR", "ALUMNO")

                // Chat del asistente (academ.ia): cualquier usuario logueado
                .requestMatchers("/api/chat/**").authenticated()

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}