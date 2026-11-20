package com.example.huertoHogar_Backend.seguridad;

import com.example.huertoHogar_Backend.usuario.UsuarioModel;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRETO =
            "secreto-de-prueba-con-mas-de-32-caracteres-1234";

    private UsuarioModel usuario() {
        UsuarioModel u = new UsuarioModel();
        u.setUsuarioId(7L);
        u.setCorreo("admin@duoc.cl");
        u.setRol("Administrador");
        return u;
    }

    @Test
    void generaUnTokenQueSePuedeLeer() {
        JwtService jwt = new JwtService(SECRETO, 60);

        Optional<Claims> datos = jwt.leer(jwt.generar(usuario()));

        assertTrue(datos.isPresent());
        assertEquals("7", datos.get().getSubject());
        assertEquals("admin@duoc.cl", datos.get().get("correo"));
        assertEquals("Administrador", datos.get().get("rol"));
    }

    @Test
    void rechazaUnTokenAlteradoOBasura() {
        JwtService jwt = new JwtService(SECRETO, 60);
        String token = jwt.generar(usuario());

        assertTrue(jwt.leer(token + "x").isEmpty());
        assertTrue(jwt.leer("esto-no-es-un-token").isEmpty());
    }

    @Test
    void rechazaUnTokenFirmadoConOtroSecreto() {
        JwtService otro = new JwtService(
                "otro-secreto-distinto-con-mas-de-32-caracteres-99", 60);
        JwtService jwt = new JwtService(SECRETO, 60);

        assertTrue(jwt.leer(otro.generar(usuario())).isEmpty());
    }

    @Test
    void rechazaUnTokenExpirado() {
        JwtService jwt = new JwtService(SECRETO, -1);

        assertTrue(jwt.leer(jwt.generar(usuario())).isEmpty());
    }

    @Test
    void noArrancaConUnSecretoCortoOVacio() {
        assertThrows(IllegalStateException.class,
                () -> new JwtService("corto", 60));
        assertThrows(IllegalStateException.class,
                () -> new JwtService("", 60));
    }
}
