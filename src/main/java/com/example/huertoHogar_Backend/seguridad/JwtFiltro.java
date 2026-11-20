package com.example.huertoHogar_Backend.seguridad;

import com.example.huertoHogar_Backend.usuario.UsuarioModel;
import com.example.huertoHogar_Backend.usuario.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Se ejecuta en cada peticion. Si trae "Authorization: Bearer <token>" y el token
 * es valido, deja al usuario autenticado con su rol. Si no, la peticion sigue
 * sin autenticar y Spring Security decide (401 o 403) segun la ruta.
 *
 * No es un @Component a proposito: se crea en SecurityConfig para que se
 * ejecute una sola vez, dentro de la cadena de seguridad.
 */
public class JwtFiltro extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtFiltro(JwtService jwtService,
                     UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain cadena)
            throws ServletException, IOException {

        String cabecera = request.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith(PREFIJO)
                && SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            String token = cabecera.substring(PREFIJO.length()).trim();
            autenticar(token);
        }

        cadena.doFilter(request, response);
    }

    private void autenticar(String token) {
        Optional<Claims> datos = jwtService.leer(token);

        if (datos.isEmpty()) {
            return;
        }

        long id;
        try {
            id = Long.parseLong(datos.get().getSubject());
        } catch (NumberFormatException e) {
            return;
        }

        // Se consulta la BD para que un usuario desactivado, o con otro rol,
        // no siga usando un token que todavia no expira.
        Optional<UsuarioModel> usuario = usuarioRepository.findById(id);

        if (usuario.isEmpty() || !"S".equals(usuario.get().getActivo())) {
            return;
        }

        UsuarioModel u = usuario.get();
        UsuarioAutenticado principal = new UsuarioAutenticado(
                u.getUsuarioId(), u.getCorreo(), u.getRol());

        // hasRole("Administrador") busca la autoridad "ROLE_Administrador"
        var autenticacion = new UsernamePasswordAuthenticationToken(
                principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol())));

        SecurityContextHolder.getContext()
                .setAuthentication(autenticacion);
    }
}
