package com.example.huertoHogar_Backend.auth;

import com.example.huertoHogar_Backend.auth.AuthService.LoginSolicitud;
import com.example.huertoHogar_Backend.auth.AuthService.RegistroSolicitud;
import com.example.huertoHogar_Backend.seguridad.JwtService;
import com.example.huertoHogar_Backend.usuario.UsuarioModel;
import com.example.huertoHogar_Backend.usuario.UsuarioRepository;
import com.example.huertoHogar_Backend.usuario.UsuarioService;
import com.example.huertoHogar_Backend.usuario.UsuarioService.UsuarioEntrada;
import com.example.huertoHogar_Backend.usuario.UsuarioService.UsuarioRespuesta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UsuarioRepository repository;
    private UsuarioService usuarioService;
    private PasswordEncoder encoder;
    private AuthService auth;

    @BeforeEach
    void preparar() {
        repository = mock(UsuarioRepository.class);
        usuarioService = mock(UsuarioService.class);
        encoder = new BCryptPasswordEncoder();
        JwtService jwt = new JwtService(
                "secreto-de-prueba-con-mas-de-32-caracteres-1234", 60);
        auth = new AuthService(repository, usuarioService, encoder, jwt);
    }

    private UsuarioModel usuario(String activo) {
        UsuarioModel u = new UsuarioModel();
        u.setUsuarioId(1L);
        u.setCorreo("cliente@gmail.com");
        u.setContrasena(encoder.encode("Cliente2026!"));
        u.setRol("Cliente");
        u.setActivo(activo);
        return u;
    }

    private UsuarioRespuesta respuesta(String rol) {
        return new UsuarioRespuesta(1L, "123456785", "Ana", "Perez",
                "cliente@gmail.com", null, null, null, null, rol, "S");
    }

    private void assertNoAutorizado(Executable accion) {
        ResponseStatusException e = assertThrows(
                ResponseStatusException.class, accion);
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
    }

    @Test
    void loginCorrectoEntregaTokenYUsuario() {
        when(repository.findByCorreoIgnoreCase("cliente@gmail.com"))
                .thenReturn(Optional.of(usuario("S")));
        when(usuarioService.buscar(1L)).thenReturn(respuesta("Cliente"));

        var res = auth.login(
                new LoginSolicitud("cliente@gmail.com", "Cliente2026!"));

        assertEquals("Bearer", res.tipo());
        assertFalse(res.token().isBlank());
        assertEquals("Cliente", res.usuario().rol());
    }

    @Test
    void loginConContrasenaIncorrectaDa401() {
        when(repository.findByCorreoIgnoreCase("cliente@gmail.com"))
                .thenReturn(Optional.of(usuario("S")));

        assertNoAutorizado(() -> auth.login(
                new LoginSolicitud("cliente@gmail.com", "otra-clave")));
    }

    @Test
    void loginConCorreoInexistenteDa401() {
        when(repository.findByCorreoIgnoreCase(any()))
                .thenReturn(Optional.empty());

        assertNoAutorizado(() -> auth.login(
                new LoginSolicitud("nadie@gmail.com", "Cliente2026!")));
    }

    @Test
    void loginDeUsuarioDesactivadoDa401() {
        when(repository.findByCorreoIgnoreCase("cliente@gmail.com"))
                .thenReturn(Optional.of(usuario("N")));

        assertNoAutorizado(() -> auth.login(
                new LoginSolicitud("cliente@gmail.com", "Cliente2026!")));
    }

    @Test
    void registroSiempreCreaUnCliente() {
        when(usuarioService.crear(any())).thenReturn(respuesta("Cliente"));
        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario("S")));
        when(usuarioService.buscar(1L)).thenReturn(respuesta("Cliente"));

        auth.registrar(new RegistroSolicitud("12345678-5", "Ana", "Perez",
                "cliente@gmail.com", "Cliente2026!", null, null, null, null));

        ArgumentCaptor<UsuarioEntrada> enviado =
                ArgumentCaptor.forClass(UsuarioEntrada.class);
        verify(usuarioService).crear(enviado.capture());
        assertEquals("Cliente", enviado.getValue().rol());
    }
}
