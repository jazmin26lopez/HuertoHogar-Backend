
package com.example.huertoHogar_Backend.boleta;

import com.example.huertoHogar_Backend.producto.ProductoModel;
import com.example.huertoHogar_Backend.producto.ProductoRepository;
import com.example.huertoHogar_Backend.seguridad.UsuarioAutenticado;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;

    @Mock
    private DetalleBoletaRepository detalleRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private BoletaService service;

    private ProductoModel producto;

    @BeforeEach
    void preparar() {
        producto = new ProductoModel();
        producto.setProductoId(1L);
        producto.setCodigo("PROD001");
        producto.setNombre("Manzana");
        producto.setPrecio(new BigDecimal("1000"));
        producto.setStock(10);
        producto.setDescuentoOferta(0);
        producto.setActivo("S");

        UsuarioAutenticado usuario = new UsuarioAutenticado(
                7L, "cliente@gmail.com", "Cliente");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        usuario, null, List.of())
        );
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private BoletaService.SolicitudCompra solicitud(
            boolean pagoAprobado, String entrega, int cantidad) {

        return new BoletaService.SolicitudCompra(
                "Jazmin",
                "Lopez",
                "cliente@gmail.com",
                "Calle 123",
                null,
                "Metropolitana",
                "Santiago",
                null,
                entrega,
                "Tarjeta",
                pagoAprobado,
                List.of(new BoletaService.ItemCompra(1L, cantidad))
        );
    }

    private void simularGuardado() {
        when(boletaRepository.saveAndFlush(any(BoletaModel.class)))
                .thenAnswer(invocacion -> {
                    BoletaModel boleta = invocacion.getArgument(0);
                    boleta.setBoletaId(20L);
                    return boleta;
                });

        when(detalleRepository.saveAllAndFlush(anyList()))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void compraAprobadaDescuentaStock() {
        when(productoRepository.bloquearPorId(1L))
                .thenReturn(Optional.of(producto));

        simularGuardado();

        var resultado = service.crearBoleta(
                solicitud(true, "retiro", 2));

        assertEquals("PAGADA", resultado.boleta().getEstado());
        assertEquals(7L, resultado.boleta().getUsuarioId());
        assertEquals(8, producto.getStock());
        assertEquals(0, new BigDecimal("2000")
                .compareTo(resultado.boleta().getTotal()));
        assertEquals(1, resultado.detalles().size());

        verify(productoRepository).save(producto);
    }

    @Test
    void pagoRechazadoNoDescuentaStock() {
        when(productoRepository.bloquearPorId(1L))
                .thenReturn(Optional.of(producto));

        simularGuardado();

        var resultado = service.crearBoleta(
                solicitud(false, "retiro", 2));

        assertEquals("FALLIDA", resultado.boleta().getEstado());
        assertEquals(10, producto.getStock());

        verify(productoRepository, never()).save(any());
    }

    @Test
    void compraConDomicilioCalculaCostoEnvio() {
        when(productoRepository.bloquearPorId(1L))
                .thenReturn(Optional.of(producto));

        simularGuardado();

        var resultado = service.crearBoleta(
                solicitud(true, "domicilio", 2));

        assertEquals(0, new BigDecimal("3000")
                .compareTo(resultado.boleta().getCostoEnvio()));

        assertEquals(0, new BigDecimal("5000")
                .compareTo(resultado.boleta().getTotal()));
    }

    @Test
    void compraAplicaDescuentoDelProducto() {
        producto.setDescuentoOferta(20);

        when(productoRepository.bloquearPorId(1L))
                .thenReturn(Optional.of(producto));

        simularGuardado();

        var resultado = service.crearBoleta(
                solicitud(true, "retiro", 2));

        assertEquals(0, new BigDecimal("800")
                .compareTo(resultado.detalles().get(0)
                        .getPrecioUnitario()));

        assertEquals(0, new BigDecimal("1600")
                .compareTo(resultado.boleta().getTotal()));
    }

    @Test
    void stockInsuficienteGeneraConflicto() {
        producto.setStock(1);

        when(productoRepository.bloquearPorId(1L))
                .thenReturn(Optional.of(producto));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.crearBoleta(
                        solicitud(true, "retiro", 5))
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(1, producto.getStock());

        verify(boletaRepository, never()).saveAndFlush(any());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void productoInexistenteGenera404() {
        when(productoRepository.bloquearPorId(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.crearBoleta(
                        solicitud(true, "retiro", 1))
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());

        verify(boletaRepository, never()).saveAndFlush(any());
    }

    @Test
    void productoDesactivadoRechazaCompra() {
        producto.setActivo("N");

        when(productoRepository.bloquearPorId(1L))
                .thenReturn(Optional.of(producto));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.crearBoleta(
                        solicitud(true, "retiro", 1))
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    @Test
    void cantidadInvalidaGenera400() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.crearBoleta(
                        solicitud(true, "retiro", 0))
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());

        verifyNoInteractions(productoRepository);
    }

    @Test
    void productoRepetidoGenera400() {
        var solicitud = new BoletaService.SolicitudCompra(
                "Jazmin", "Lopez", "cliente@gmail.com",
                "Calle 123", null, "Metropolitana",
                "Santiago", null, "retiro", "Tarjeta",
                true,
                List.of(
                        new BoletaService.ItemCompra(1L, 1),
                        new BoletaService.ItemCompra(1L, 2)
                )
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.crearBoleta(solicitud)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());

        verifyNoInteractions(productoRepository);
    }

    @Test
    void listarBoletasDevuelveDetalles() {
        BoletaModel boleta = new BoletaModel();
        boleta.setBoletaId(20L);

        DetalleBoletaModel detalle = new DetalleBoletaModel();
        detalle.setBoletaId(20L);
        detalle.setProductoId(1L);

        when(boletaRepository.findAll())
                .thenReturn(List.of(boleta));

        when(detalleRepository.findByBoletaId(20L))
                .thenReturn(List.of(detalle));

        var resultado = service.listarBoletas();

        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).detalles().size());
    }

    @Test
    void buscarBoletaInexistenteGenera404() {
        when(boletaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
}
