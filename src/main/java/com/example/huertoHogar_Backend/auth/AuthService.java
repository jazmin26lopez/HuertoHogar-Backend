package com.example.huertoHogar_Backend.auth;

import com.example.huertoHogar_Backend.seguridad.JwtService;
import com.example.huertoHogar_Backend.usuario.UsuarioModel;
import com.example.huertoHogar_Backend.usuario.UsuarioRepository;
import com.example.huertoHogar_Backend.usuario.UsuarioService;
import com.example.huertoHogar_Backend.usuario.UsuarioService.UsuarioEntrada;
import com.example.huertoHogar_Backend.usuario.UsuarioService.UsuarioRespuesta;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       UsuarioService usuarioService,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public record LoginSolicitud(
            @NotBlank(message = "El correo es obligatorio") String correo,
            @NotBlank(message = "La contrasena es obligatoria") String contrasena
    ) {}

    /** Registro publico: no trae "rol", porque quien se registra siempre es Cliente. */
    public record RegistroSolicitud(
            String run,
            String nombre,
            String apellidos,
            String correo,
            String contrasena,
            LocalDate fechaNacimiento,
            String region,
            String comuna,
            String direccion
    ) {}

    public record RespuestaAuth(
            String token,
            String tipo,
            long expiraEnMinutos,
            UsuarioRespuesta usuario
    ) {}

    @Transactional(readOnly = true)
    public RespuestaAuth login(LoginSolicitud datos) {
        // Mismo mensaje si el correo no existe, la clave es incorrecta o
        // el usuario esta desactivado: asi no se revela cual de los casos fue.
        UsuarioModel usuario = usuarioRepository
                .findByCorreoIgnoreCase(datos.correo().trim())
                .filter(u -> "S".equals(u.getActivo()))
                .filter(u -> passwordEncoder.matches(
                        datos.contrasena(), u.getContrasena()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));

        return respuesta(usuario);
    }

    @Transactional
    public RespuestaAuth registrar(RegistroSolicitud datos) {
        // Se reutilizan las validaciones y el cifrado de UsuarioService.
        // El rol se fija aqui, nunca viene del cliente.
        UsuarioRespuesta creado = usuarioService.crear(new UsuarioEntrada(
                datos.run(), datos.nombre(), datos.apellidos(),
                datos.correo(), datos.contrasena(),
                datos.fechaNacimiento(), datos.region(),
                datos.comuna(), datos.direccion(), "Cliente"));

        UsuarioModel usuario = usuarioRepository
                .findById(creado.usuarioId())
                .orElseThrow();

        return respuesta(usuario);
    }

    /** Datos del usuario logueado. */
    @Transactional(readOnly = true)
    public UsuarioRespuesta perfil(Long usuarioId) {
        return usuarioService.buscar(usuarioId);
    }

    /** Entrega un token nuevo a quien aun tiene uno valido. */
    @Transactional(readOnly = true)
    public RespuestaAuth renovar(Long usuarioId) {
        UsuarioModel usuario = usuarioRepository.findById(usuarioId)
                .filter(u -> "S".equals(u.getActivo()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Sesion no valida"));

        return respuesta(usuario);
    }

    private RespuestaAuth respuesta(UsuarioModel usuario) {
        return new RespuestaAuth(
                jwtService.generar(usuario),
                "Bearer",
                jwtService.getMinutosDeVida(),
                usuarioService.buscar(usuario.getUsuarioId()));
    }
}
