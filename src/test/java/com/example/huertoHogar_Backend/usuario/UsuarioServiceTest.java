
package com.example.huertoHogar_Backend.usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    private UsuarioModel usuario;

    @BeforeEach
    void preparar() {
        usuario = new UsuarioModel();
        usuario.setUsuarioId(1L);
        usuario.setRun("123456785");
        usuario.setNombre("Jazmin");
        usuario.setApellidos("Lopez");
        usuario.setCorreo("cliente@gmail.com");
        usuario.setContrasena("hash-bcrypt-anterior");
        usuario.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        usuario.setRegion("Metropolitana");
        usuario.setComuna("Santiago");
        usuario.setDireccion("Calle 123");
        usuario.setRol("Cliente");
        usuario.setActivo("S");
    }

    private UsuarioService.UsuarioEntrada datosValidos() {
        return new UsuarioService.UsuarioEntrada(
                "12.345.678-5",
                "Jazmin",
                "Lopez",
                "CLIENTE@gmail.com",
                "ClaveSegura123",
                LocalDate.of(2000, 1, 1),
                "Metropolitana",
                "Santiago",
                "Calle 123",
                "Cliente"
        );
    }

    private UsuarioService.UsuarioEntrada datos(
            String run,
            String nombre,
            String correo,
            String contrasena,
            String rol) {

        return new UsuarioService.UsuarioEntrada(
                run,
                nombre,
                "Lopez",
                correo,
                contrasena,
                null,
                "Metropolitana",
                "Santiago",
                "Calle 123",
                rol
        );
    }

    private void simularGuardado() {
        when(repository.saveAndFlush(any(UsuarioModel.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));
    }

    private void verificarError(
            HttpStatus estado, Runnable accion) {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                accion::run
        );

        assertEquals(estado, error.getStatusCode());
    }

    // CONSULTAS

    @Test
    void listarUsuariosCorrectamente() {
        when(repository.findAll())
                .thenReturn(List.of(usuario));

        var resultado = service.listar();

        assertEquals(1, resultado.size());
        assertEquals("Jazmin", resultado.get(0).nombre());
        assertEquals("Cliente", resultado.get(0).rol());

        verify(repository).findAll();
    }

    @Test
    void listarUsuariosVacios() {
        when(repository.findAll())
                .thenReturn(List.of());

        assertTrue(service.listar().isEmpty());
    }

    @Test
    void buscarUsuarioExistente() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        var resultado = service.buscar(1L);

        assertEquals(1L, resultado.usuarioId());
        assertEquals("cliente@gmail.com", resultado.correo());
        assertEquals("S", resultado.activo());
    }

    @Test
    void buscarUsuarioInexistenteGenera404() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        verificarError(
                HttpStatus.NOT_FOUND,
                () -> service.buscar(99L)
        );
    }

    // CREACION

    @Test
    void crearUsuarioCorrectamenteConBCrypt() {
        when(passwordEncoder.encode("ClaveSegura123"))
                .thenReturn("hash-bcrypt-nuevo");

        simularGuardado();

        var resultado = service.crear(datosValidos());

        assertEquals("123456785", resultado.run());
        assertEquals("cliente@gmail.com", resultado.correo());
        assertEquals("Cliente", resultado.rol());
        assertEquals("S", resultado.activo());

        verify(passwordEncoder).encode("ClaveSegura123");
        verify(repository).saveAndFlush(argThat(u ->
                "hash-bcrypt-nuevo".equals(u.getContrasena())
                        && "cliente@gmail.com".equals(u.getCorreo())
                        && "123456785".equals(u.getRun())
        ));
    }

    @Test
    void crearUsuarioConRunDuplicadoGenera409() {
        when(repository.existsByRun("123456785"))
                .thenReturn(true);

        verificarError(
                HttpStatus.CONFLICT,
                () -> service.crear(datosValidos())
        );

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void crearUsuarioConCorreoDuplicadoGenera409() {
        when(repository.existsByCorreoIgnoreCase(
                "cliente@gmail.com"))
                .thenReturn(true);

        verificarError(
                HttpStatus.CONFLICT,
                () -> service.crear(datosValidos())
        );

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void crearUsuarioConRunInvalidoGenera400() {
        var entrada = datos(
                "123",
                "Jazmin",
                "cliente@gmail.com",
                "ClaveSegura123",
                "Cliente"
        );

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crear(entrada)
        );
    }

    @Test
    void crearUsuarioConCorreoInvalidoGenera400() {
        var entrada = datos(
                "123456785",
                "Jazmin",
                "correo-invalido",
                "ClaveSegura123",
                "Cliente"
        );

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crear(entrada)
        );
    }

    @Test
    void crearUsuarioConRolInvalidoGenera400() {
        var entrada = datos(
                "123456785",
                "Jazmin",
                "cliente@gmail.com",
                "ClaveSegura123",
                "SuperAdministrador"
        );

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crear(entrada)
        );
    }

    @Test
    void crearUsuarioConContrasenaCortaGenera400() {
        var entrada = datos(
                "123456785",
                "Jazmin",
                "cliente@gmail.com",
                "1234",
                "Cliente"
        );

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crear(entrada)
        );
    }

    @Test
    void crearUsuarioSinNombreGenera400() {
        var entrada = datos(
                "123456785",
                "",
                "cliente@gmail.com",
                "ClaveSegura123",
                "Cliente"
        );

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crear(entrada)
        );
    }

    // ACTUALIZACION

    @Test
    void actualizarUsuarioCorrectamente() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        simularGuardado();

        var entrada = datos(
                "12.345.678-5",
                "Jazmin Actualizada",
                "nuevo@gmail.com",
                null,
                "Vendedor"
        );

        var resultado = service.actualizar(1L, entrada);

        assertEquals("Jazmin Actualizada", resultado.nombre());
        assertEquals("nuevo@gmail.com", resultado.correo());
        assertEquals("Vendedor", resultado.rol());

        assertEquals(
                "hash-bcrypt-anterior",
                usuario.getContrasena()
        );

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void actualizarUsuarioConCorreoDuplicadoGenera409() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.existsByCorreoIgnoreCaseAndUsuarioIdNot(
                "cliente@gmail.com", 1L))
                .thenReturn(true);

        verificarError(
                HttpStatus.CONFLICT,
                () -> service.actualizar(1L, datosValidos())
        );

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void actualizarUsuarioConOtroRunGenera400() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        var entrada = datos(
                "111111111",
                "Jazmin",
                "cliente@gmail.com",
                null,
                "Cliente"
        );

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.actualizar(1L, entrada)
        );

        verify(repository, never()).saveAndFlush(any());
    }

    // DESACTIVACION

    @Test
    void desactivarUsuarioCorrectamente() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        simularGuardado();

        service.desactivar(1L);

        assertEquals("N", usuario.getActivo());

        verify(repository).saveAndFlush(usuario);
    }
}
