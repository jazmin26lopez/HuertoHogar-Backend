
package com.example.huertoHogar_Backend.config;

import com.example.huertoHogar_Backend.seguridad.JwtFiltro;
import com.example.huertoHogar_Backend.seguridad.JwtService;
import com.example.huertoHogar_Backend.usuario.UsuarioRepository;
import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.List;

/**
 * Seguridad de la API: sesion sin estado (JWT) y permisos por rol.
 *
 * Roles: Administrador, Vendedor y Cliente.
 */
@Configuration
@EnableMethodSecurity

public class SecurityConfig {

    private static final String ADMIN = "Administrador";
    private static final String VENDEDOR = "Vendedor";

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            UsuarioRepository usuarioRepository)
            throws Exception {

        http
                // Permitir solicitudes desde React
                .cors(Customizer.withDefaults())

                // API REST sin sesiones de servidor
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // CSRF desactivado solo para la API REST
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )

                // Sin token (o token invalido): 401 en vez de 403
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                // Reglas de acceso
                .authorizeHttpRequests(auth -> auth

                        // Permitir mostrar errores de Spring
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error").permitAll()

                        // Swagger UI y documentacion OpenAPI
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Registro e inicio de sesion: publicos
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/registro"
                        ).permitAll()

                        // Consultas publicas de categorias y productos
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/categorias",
                                "/api/v1/categorias/**",
                                "/api/v1/productos",
                                "/api/v1/productos/**"
                        ).permitAll()

                        // Crear, editar y eliminar productos y categorias:
                        // solo Administrador (el Vendedor unicamente consulta)
                        .requestMatchers(
                                "/api/v1/categorias",
                                "/api/v1/categorias/**",
                                "/api/v1/productos",
                                "/api/v1/productos/**"
                        ).hasRole(ADMIN)

                        // Gestion de usuarios: solo Administrador
                        .requestMatchers("/api/v1/usuarios", "/api/v1/usuarios/**")
                        .hasRole(ADMIN)

                        // Ver las boletas: Administrador y Vendedor
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/boletas",
                                "/api/v1/boletas/**"
                        ).hasAnyRole(ADMIN, VENDEDOR)

                        // Otras operaciones de la API (comprar, /auth/me,
                        // /auth/refresh) requieren haber iniciado sesion
                        .requestMatchers("/api/**").authenticated()

                        // Bloquear las demas rutas
                        .anyRequest().denyAll()
                )

                // Lee el token JWT de cada peticion
                .addFilterBefore(
                        new JwtFiltro(jwtService, usuarioRepository),
                        UsernamePasswordAuthenticationFilter.class)

                // Sin login por formulario ni autenticacion basica
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Frontend React
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
