
package com.example.huertoHogar_Backend.producto;

import com.example.huertoHogar_Backend.categoria.CategoriaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService service;

    private ProductoModel producto;

    @BeforeEach
    void preparar() {
        producto = new ProductoModel();
        producto.setProductoId(1L);
        producto.setCodigo("PROD001");
        producto.setNombre("Manzana");
        producto.setCategoriaId(1L);
        producto.setPrecio(new BigDecimal("1500"));
        producto.setStock(20);
        producto.setUnidadMedida("kg");
        producto.setOrigen("Chile");
        producto.setDescripcion("Manzanas frescas");
        producto.setImagen("manzana.jpg");
        producto.setDescuentoOferta(10);
        producto.setActivo("S");
    }

    private void categoriaExistente() {
        when(categoriaRepository.existsById(1L))
                .thenReturn(true);
    }

    private void simularGuardado() {
        when(repository.saveAndFlush(any(ProductoModel.class)))
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
    void listarProductosActivos() {
        when(repository.findByActivo("S"))
                .thenReturn(List.of(producto));

        List<ProductoModel> resultado =
                service.listarProductos();

        assertEquals(1, resultado.size());
        assertEquals("Manzana", resultado.get(0).getNombre());

        verify(repository).findByActivo("S");
    }

    @Test
    void buscarProductoExistente() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(producto));

        ProductoModel resultado = service.buscarPorId(1L);

        assertEquals(1L, resultado.getProductoId());
        assertEquals("PROD001", resultado.getCodigo());
    }

    @Test
    void buscarProductoInexistenteGenera404() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        verificarError(
                HttpStatus.NOT_FOUND,
                () -> service.buscarPorId(99L)
        );
    }

    @Test
    void buscarProductoDesactivadoGenera404() {
        producto.setActivo("N");

        when(repository.findById(1L))
                .thenReturn(Optional.of(producto));

        verificarError(
                HttpStatus.NOT_FOUND,
                () -> service.buscarPorId(1L)
        );
    }

    @Test
    void listarProductosPorCategoria() {
        when(repository.findByCategoriaIdAndActivo(1L, "S"))
                .thenReturn(List.of(producto));

        List<ProductoModel> resultado =
                service.listarPorCategoria(1L);

        assertEquals(1, resultado.size());

        verify(repository)
                .findByCategoriaIdAndActivo(1L, "S");
    }

    // CREACION

    @Test
    void crearProductoCorrectamente() {
        categoriaExistente();
        simularGuardado();

        producto.setCodigo(" PROD001 ");
        producto.setNombre(" Manzana ");

        ProductoModel resultado =
                service.crearProducto(producto);

        assertEquals("PROD001", resultado.getCodigo());
        assertEquals("Manzana", resultado.getNombre());
        assertEquals("S", resultado.getActivo());
        assertNull(resultado.getProductoId());

        verify(repository).saveAndFlush(producto);
    }

    @Test
    void crearProductoConCodigoDuplicadoGenera409() {
        categoriaExistente();

        when(repository.existsByCodigoIgnoreCase("PROD001"))
                .thenReturn(true);

        verificarError(
                HttpStatus.CONFLICT,
                () -> service.crearProducto(producto)
        );

        verify(repository, never())
                .saveAndFlush(any());
    }

    @Test
    void crearProductoConNombreVacioGenera400() {
        producto.setNombre(" ");

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crearProducto(producto)
        );

        verifyNoInteractions(categoriaRepository);
    }

    @Test
    void crearProductoSinCategoriaGenera400() {
        producto.setCategoriaId(null);

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crearProducto(producto)
        );
    }

    @Test
    void crearProductoConCategoriaInexistenteGenera400() {
        when(categoriaRepository.existsById(1L))
                .thenReturn(false);

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crearProducto(producto)
        );
    }

    @Test
    void crearProductoConPrecioNegativoGenera400() {
        categoriaExistente();
        producto.setPrecio(new BigDecimal("-100"));

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crearProducto(producto)
        );
    }

    @Test
    void crearProductoConPrecioDeTresDecimalesGenera400() {
        categoriaExistente();
        producto.setPrecio(new BigDecimal("100.123"));

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crearProducto(producto)
        );
    }

    @Test
    void crearProductoConStockNegativoGenera400() {
        categoriaExistente();
        producto.setStock(-5);

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crearProducto(producto)
        );
    }

    @Test
    void crearProductoConDescuentoInvalidoGenera400() {
        categoriaExistente();
        producto.setDescuentoOferta(110);

        verificarError(
                HttpStatus.BAD_REQUEST,
                () -> service.crearProducto(producto)
        );
    }

    // ACTUALIZACION

    @Test
    void actualizarProductoCorrectamente() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(producto));

        categoriaExistente();
        simularGuardado();

        ProductoModel nuevosDatos = new ProductoModel();
        nuevosDatos.setCodigo("NUEVO001");
        nuevosDatos.setNombre("Pera");
        nuevosDatos.setCategoriaId(1L);
        nuevosDatos.setPrecio(new BigDecimal("2500"));
        nuevosDatos.setStock(30);
        nuevosDatos.setDescuentoOferta(5);

        ProductoModel resultado =
                service.actualizarProducto(1L, nuevosDatos);

        assertEquals("NUEVO001", resultado.getCodigo());
        assertEquals("Pera", resultado.getNombre());
        assertEquals(30, resultado.getStock());
        assertEquals(5, resultado.getDescuentoOferta());

        verify(repository).saveAndFlush(producto);
    }

    @Test
    void actualizarConCodigoDuplicadoGenera409() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(producto));

        categoriaExistente();

        when(repository
                .existsByCodigoIgnoreCaseAndProductoIdNot(
                        "PROD001", 1L))
                .thenReturn(true);

        verificarError(
                HttpStatus.CONFLICT,
                () -> service.actualizarProducto(1L, producto)
        );

        verify(repository, never())
                .saveAndFlush(any());
    }

    // ELIMINACION LOGICA

    @Test
    void desactivarProductoCorrectamente() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(producto));

        simularGuardado();

        service.desactivarProducto(1L);

        assertEquals("N", producto.getActivo());

        verify(repository).saveAndFlush(producto);
    }
}
