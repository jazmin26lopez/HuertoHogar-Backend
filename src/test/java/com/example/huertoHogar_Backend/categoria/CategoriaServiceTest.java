
package com.example.huertoHogar_Backend.categoria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repository;

    @InjectMocks
    private CategoriaService service;

    private CategoriaModel categoria;

    @BeforeEach
    void preparar() {
        categoria = new CategoriaModel();
        categoria.setCategoriaId(1L);
        categoria.setNombre("Frutas");
        categoria.setDescripcion("Frutas frescas");
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
    void listarCategoriasCorrectamente() {
        when(repository.findAll())
                .thenReturn(List.of(categoria));

        List<CategoriaModel> resultado =
                service.listarCategorias();

        assertEquals(1, resultado.size());
        assertEquals("Frutas", resultado.get(0).getNombre());

        verify(repository).findAll();
    }

    @Test
    void listarCategoriasVacias() {
        when(repository.findAll())
                .thenReturn(List.of());

        assertTrue(service.listarCategorias().isEmpty());
    }

    @Test
    void buscarCategoriaExistente() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(categoria));

        CategoriaModel resultado = service.buscarPorId(1L);

        assertEquals(1L, resultado.getCategoriaId());
        assertEquals("Frutas", resultado.getNombre());
    }

    @Test
    void buscarCategoriaInexistenteGenera404() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        verificarError(
                HttpStatus.NOT_FOUND,
                () -> service.buscarPorId(99L)
        );
    }

    // CREACION

    @Test
    void crearCategoriaCorrectamente() {
        when(repository.saveAndFlush(any(CategoriaModel.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        categoria.setNombre(" Frutas ");

        CategoriaModel resultado =
                service.crearCategoria(categoria);

        assertNull(resultado.getCategoriaId());
        assertEquals("Frutas", resultado.getNombre());

        verify(repository).saveAndFlush(categoria);
    }

    @Test
    void crearCategoriaDuplicadaGenera409() {
        when(repository.existsByNombreIgnoreCase("Frutas"))
                .thenReturn(true);

        verificarError(
                HttpStatus.CONFLICT,
                () -> service.crearCategoria(categoria)
        );

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void crearCategoriaSinNombreGenera400() {
        categoria.setNombre(" ");

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crearCategoria(categoria)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void crearCategoriaConNombreMuyLargoGenera400() {
        categoria.setNombre("A".repeat(101));

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crearCategoria(categoria)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void crearCategoriaConDescripcionMuyLargaGenera400() {
        categoria.setDescripcion("A".repeat(501));

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crearCategoria(categoria)
        );

        verifyNoInteractions(repository);
    }

    // ACTUALIZACION

    @Test
    void actualizarCategoriaCorrectamente() {
        CategoriaModel nuevosDatos = new CategoriaModel();
        nuevosDatos.setNombre(" Verduras ");
        nuevosDatos.setDescripcion("Verduras de temporada");

        when(repository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(repository.saveAndFlush(any(CategoriaModel.class)))
                .thenAnswer(invocacion ->
                        invocacion.getArgument(0));

        CategoriaModel resultado =
                service.actualizarCategoria(1L, nuevosDatos);

        assertEquals("Verduras", resultado.getNombre());
        assertEquals(
                "Verduras de temporada",
                resultado.getDescripcion()
        );

        verify(repository).saveAndFlush(categoria);
    }

    @Test
    void actualizarCategoriaDuplicadaGenera409() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(repository
                .existsByNombreIgnoreCaseAndCategoriaIdNot(
                        "Frutas", 1L))
                .thenReturn(true);

        verificarError(
                HttpStatus.CONFLICT,
                () -> service.actualizarCategoria(1L, categoria)
        );

        verify(repository, never()).saveAndFlush(any());
    }

    // ELIMINACION

    @Test
    void eliminarCategoriaCorrectamente() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(categoria));

        service.eliminarCategoria(1L);

        verify(repository).delete(categoria);
        verify(repository).flush();
    }

    @Test
    void eliminarCategoriaInexistenteGenera404() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        verificarError(
                HttpStatus.NOT_FOUND,
                () -> service.eliminarCategoria(99L)
        );

        verify(repository, never()).delete(any());
    }

    @Test
    void eliminarCategoriaConProductosGenera409() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(categoria));

        doThrow(new DataIntegrityViolationException(
                "La categoria tiene productos asociados"))
                .when(repository).flush();

        verificarError(
                HttpStatus.CONFLICT,
                () -> service.eliminarCategoria(1L)
        );

        verify(repository).delete(categoria);
        verify(repository).flush();
    }
}
