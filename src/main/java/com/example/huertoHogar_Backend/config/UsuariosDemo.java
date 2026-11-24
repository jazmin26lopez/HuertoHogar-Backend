package com.example.huertoHogar_Backend.config;

import com.example.huertoHogar_Backend.usuario.UsuarioModel;
import com.example.huertoHogar_Backend.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea al arrancar tres cuentas de demostracion (una por rol) si todavia no
 * existen. Las contrasenas se guardan con BCrypt; por eso se crean desde
 * Spring y no desde un script SQL.
 *
 * Se desactiva con app.demo.usuarios=false.
 */
@Component
@ConditionalOnProperty(name = "app.demo.usuarios",
        havingValue = "true", matchIfMissing = true)
public class UsuariosDemo implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(UsuariosDemo.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuariosDemo(UsuarioRepository repository,
                        PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("111111111", "Admin", "HuertoHogar",
                "admin@duoc.cl", "Admin2026!", "Administrador");
        crearSiNoExiste("222222222", "Vendedor", "HuertoHogar",
                "vendedor@duoc.cl", "Vendedor2026!", "Vendedor");
        crearSiNoExiste("123456785", "Cliente", "HuertoHogar",
                "cliente@gmail.com", "Cliente2026!", "Cliente");
    }

    private void crearSiNoExiste(String run, String nombre,
                                 String apellidos, String correo,
                                 String contrasena, String rol) {

        if (repository.existsByCorreoIgnoreCase(correo)
                || repository.existsByRun(run)) {
            return;
        }

        UsuarioModel usuario = new UsuarioModel();
        usuario.setRun(run);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(contrasena));
        usuario.setRol(rol);
        usuario.setActivo("S");

        repository.save(usuario);
        log.info("Usuario de demostracion creado: {} ({})", correo, rol);
    }
}
