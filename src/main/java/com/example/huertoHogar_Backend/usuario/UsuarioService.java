
package com.example.huertoHogar_Backend.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public record UsuarioEntrada(
            String run,
            String nombre,
            String apellidos,
            String correo,
            String contrasena,
            LocalDate fechaNacimiento,
            String region,
            String comuna,
            String direccion,
            String rol
    ) {}

    public record UsuarioRespuesta(
            Long usuarioId,
            String run,
            String nombre,
            String apellidos,
            String correo,
            LocalDate fechaNacimiento,
            String region,
            String comuna,
            String direccion,
            String rol,
            String activo
    ) {}

    @Transactional(readOnly = true)
    public List<UsuarioRespuesta> listar() {
        return repository.findAll().stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioRespuesta buscar(Long id) {
        return convertirRespuesta(buscarEntidad(id));
    }

    @Transactional
    public UsuarioRespuesta crear(UsuarioEntrada datos) {
        validarDatos(datos, true);

        String run = normalizarRun(datos.run());
        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);

        if (repository.existsByRun(run)) {
            throw error(HttpStatus.CONFLICT, "RUN ya registrado");
        }

        if (repository.existsByCorreoIgnoreCase(correo)) {
            throw error(HttpStatus.CONFLICT, "Correo ya registrado");
        }

        UsuarioModel usuario = new UsuarioModel();

        usuario.setRun(run);
        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setContrasena(
                passwordEncoder.encode(datos.contrasena()));
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setRegion(datos.region());
        usuario.setComuna(datos.comuna());
        usuario.setDireccion(datos.direccion());
        usuario.setRol(datos.rol());
        usuario.setActivo("S");

        return convertirRespuesta(repository.saveAndFlush(usuario));
    }

    @Transactional
    public UsuarioRespuesta actualizar(
            Long id, UsuarioEntrada datos) {

        validarDatos(datos, false);

        UsuarioModel usuario = buscarEntidad(id);
        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);

        // El RUN no se modifica
        if (!normalizarRun(datos.run()).equals(usuario.getRun())) {
            throw error(HttpStatus.BAD_REQUEST,
                    "No se permite modificar el RUN");
        }

        if (repository.existsByCorreoIgnoreCaseAndUsuarioIdNot(
                correo, id)) {
            throw error(HttpStatus.CONFLICT, "Correo ya registrado");
        }

        usuario.setNombre(datos.nombre().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setFechaNacimiento(datos.fechaNacimiento());
        usuario.setRegion(datos.region());
        usuario.setComuna(datos.comuna());
        usuario.setDireccion(datos.direccion());
        usuario.setRol(datos.rol());

        if (datos.contrasena() != null &&
                !datos.contrasena().isBlank()) {
            validarContrasena(datos.contrasena());
            usuario.setContrasena(
                    passwordEncoder.encode(datos.contrasena()));
        }

        return convertirRespuesta(repository.saveAndFlush(usuario));
    }

    @Transactional
    public void desactivar(Long id) {
        UsuarioModel usuario = buscarEntidad(id);
        usuario.setActivo("N");
        repository.saveAndFlush(usuario);
    }

    private UsuarioModel buscarEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));
    }

    private UsuarioRespuesta convertirRespuesta(UsuarioModel u) {
        return new UsuarioRespuesta(
                u.getUsuarioId(),
                u.getRun(),
                u.getNombre(),
                u.getApellidos(),
                u.getCorreo(),
                u.getFechaNacimiento(),
                u.getRegion(),
                u.getComuna(),
                u.getDireccion(),
                u.getRol(),
                u.getActivo()
        );
    }

    private void validarDatos(UsuarioEntrada d, boolean crear) {

        if (d == null) {
            throw error(HttpStatus.BAD_REQUEST, "Datos obligatorios");
        }

        String run = normalizarRun(d.run());

        if (!run.matches("[0-9]{7,8}[0-9K]")) {
            throw error(HttpStatus.BAD_REQUEST, "RUN invalido");
        }

        validarTexto(d.nombre(), 50, "Nombre");
        validarTexto(d.apellidos(), 100, "Apellidos");
        validarTexto(d.correo(), 100, "Correo");

        String correo = d.correo().trim().toLowerCase(Locale.ROOT);

        if (!correo.matches(
                "^[A-Za-z0-9._%+\\-]+@(gmail\\.com|duoc\\.cl|profesor\\.duoc\\.cl)$")) {
            throw error(HttpStatus.BAD_REQUEST,
                    "Dominio de correo no autorizado");
        }

        if (d.rol() == null ||
                !List.of("Cliente", "Vendedor", "Administrador")
                        .contains(d.rol())) {
            throw error(HttpStatus.BAD_REQUEST, "Rol invalido");
        }

        if (crear) {
            validarContrasena(d.contrasena());
        }

        validarOpcional(d.region(), 80, "Region");
        validarOpcional(d.comuna(), 80, "Comuna");
        validarOpcional(d.direccion(), 300, "Direccion");
    }

    private String normalizarRun(String run) {
        return run == null ? ""
                : run.replace(".", "")
                .replace("-", "")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private void validarContrasena(String clave) {
        if (clave == null || clave.length() < 8 ||
                clave.length() > 72) {
            throw error(HttpStatus.BAD_REQUEST,
                    "La contrasena debe tener entre 8 y 72 caracteres");
        }
    }

    private void validarTexto(
            String valor, int maximo, String campo) {
        if (valor == null || valor.isBlank() ||
                valor.trim().length() > maximo) {
            throw error(HttpStatus.BAD_REQUEST,
                    "Campo invalido: " + campo);
        }
    }

    private void validarOpcional(
            String valor, int maximo, String campo) {
        if (valor != null && valor.length() > maximo) {
            throw error(HttpStatus.BAD_REQUEST,
                    "Campo invalido: " + campo);
        }
    }

    private ResponseStatusException error(
            HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }
}
