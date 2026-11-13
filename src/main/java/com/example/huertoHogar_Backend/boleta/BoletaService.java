
package com.example.huertoHogar_Backend.boleta;

import com.example.huertoHogar_Backend.producto.ProductoModel;
import com.example.huertoHogar_Backend.producto.ProductoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final DetalleBoletaRepository detalleRepository;
    private final ProductoRepository productoRepository;

    // Tarifa provisional para compras con despacho
    private static final BigDecimal ENVIO_DOMICILIO =
            new BigDecimal("3000");

    public BoletaService(
            BoletaRepository boletaRepository,
            DetalleBoletaRepository detalleRepository,
            ProductoRepository productoRepository) {

        this.boletaRepository = boletaRepository;
        this.detalleRepository = detalleRepository;
        this.productoRepository = productoRepository;
    }

    // Datos que envia React o Swagger
    public record ItemCompra(
            Long productoId,
            Integer cantidad
    ) {}

    public record SolicitudCompra(
            String nombreCliente,
            String apellidosCliente,
            String correoCliente,
            String calle,
            String departamento,
            String region,
            String comuna,
            String indicaciones,
            String tipoEntrega,
            String metodoPago,
            Boolean pagoAprobado,
            List<ItemCompra> productos
    ) {}

    // Respuesta con la boleta y sus detalles
    public record RespuestaBoleta(
            BoletaModel boleta,
            List<DetalleBoletaModel> detalles
    ) {}

    // REGISTRAR COMPRA
    @Transactional
    public RespuestaBoleta crearBoleta(SolicitudCompra solicitud) {

        validarSolicitud(solicitud);

        boolean aprobada = solicitud.pagoAprobado();
        boolean domicilio =
                "domicilio".equals(solicitud.tipoEntrega());

        // Evita repetir productos en una compra
        Set<Long> ids = new HashSet<>();

        for (ItemCompra item : solicitud.productos()) {

            if (item == null || item.productoId() == null ||
                    item.cantidad() == null ||
                    item.cantidad() <= 0) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Producto o cantidad invalida"
                );
            }

            if (!ids.add(item.productoId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No repitas productos en la misma compra"
                );
            }
        }

        // Orden fijo para reducir conflictos entre compras
        List<ItemCompra> items = new ArrayList<>(
                solicitud.productos());

        items.sort(Comparator.comparing(ItemCompra::productoId));

        List<DetalleBoletaModel> detalles = new ArrayList<>();
        BigDecimal subtotalCompra = BigDecimal.ZERO;

        for (ItemCompra item : items) {

            // Bloqueo de fila durante la transaccion
            ProductoModel producto = productoRepository
                    .bloquearPorId(item.productoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Producto no encontrado: " + item.productoId()
                    ));

            if (!"S".equals(producto.getActivo())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Producto desactivado: " + producto.getNombre()
                );
            }

            // Stock se valida para compras aprobadas
            if (aprobada && producto.getStock() < item.cantidad()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente: " + producto.getNombre()
                );
            }

            int descuento = producto.getDescuentoOferta() == null
                    ? 0 : producto.getDescuentoOferta();

            // Precio con descuento, calculado en el backend
            BigDecimal porcentaje = BigDecimal.valueOf(
                    100L - descuento);

            BigDecimal precioFinal = producto.getPrecio()
                    .multiply(porcentaje)
                    .divide(new BigDecimal("100"),
                            2, RoundingMode.HALF_UP);

            BigDecimal subtotalItem = precioFinal.multiply(
                    BigDecimal.valueOf(item.cantidad()));

            subtotalCompra = subtotalCompra.add(subtotalItem);

            DetalleBoletaModel detalle = new DetalleBoletaModel();
            detalle.setProductoId(producto.getProductoId());
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(precioFinal);
            detalle.setSubtotal(subtotalItem);

            detalles.add(detalle);

            // Descontar inventario solo si el pago fue aprobado
            if (aprobada) {
                producto.setStock(
                        producto.getStock() - item.cantidad());
                productoRepository.save(producto);
            }
        }

        BigDecimal costoEnvio = domicilio
                ? ENVIO_DOMICILIO
                : BigDecimal.ZERO;

        BigDecimal totalCompra = subtotalCompra.add(costoEnvio);

        // Crear encabezado de la boleta
        BoletaModel boleta = new BoletaModel();

        boleta.setCodigo(
                "ORDER" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase()
        );

        // Compra como invitado hasta integrar JWT
        boleta.setUsuarioId(null);

        boleta.setEstado(aprobada ? "PAGADA" : "FALLIDA");
        boleta.setNombreCliente(solicitud.nombreCliente().trim());
        boleta.setApellidosCliente(
                solicitud.apellidosCliente().trim());
        boleta.setCorreoCliente(
                solicitud.correoCliente().trim());

        boleta.setCalle(solicitud.calle().trim());
        boleta.setDepartamento(solicitud.departamento());
        boleta.setRegion(solicitud.region().trim());
        boleta.setComuna(solicitud.comuna().trim());
        boleta.setIndicaciones(solicitud.indicaciones());
        boleta.setTipoEntrega(solicitud.tipoEntrega());
        boleta.setMetodoPago(solicitud.metodoPago().trim());

        boleta.setSubtotal(subtotalCompra);
        boleta.setCostoEnvio(costoEnvio);
        boleta.setTotal(totalCompra);

        // Primero guardar encabezado para obtener ID
        BoletaModel guardada =
                boletaRepository.saveAndFlush(boleta);

        // Relacionar detalles con la boleta creada
        for (DetalleBoletaModel detalle : detalles) {
            detalle.setBoletaId(guardada.getBoletaId());
        }

        List<DetalleBoletaModel> detallesGuardados =
                detalleRepository.saveAllAndFlush(detalles);

        return new RespuestaBoleta(
                guardada, detallesGuardados);
    }

    // LISTAR TODAS LAS BOLETAS
    @Transactional(readOnly = true)
    public List<RespuestaBoleta> listarBoletas() {

        return boletaRepository.findAll()
                .stream()
                .map(this::conDetalles)
                .toList();
    }

    // CONSULTAR BOLETA POR ID
    @Transactional(readOnly = true)
    public RespuestaBoleta buscarPorId(Long id) {

        BoletaModel boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Boleta no encontrada"
                ));

        return conDetalles(boleta);
    }

    private RespuestaBoleta conDetalles(BoletaModel boleta) {

        return new RespuestaBoleta(
                boleta,
                detalleRepository.findByBoletaId(
                        boleta.getBoletaId())
        );
    }

    // VALIDACIONES DE ENTRADA
    private void validarSolicitud(SolicitudCompra s) {

        if (s == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La solicitud es obligatoria"
            );
        }

        validarTexto(s.nombreCliente(), 50, "Nombre");
        validarTexto(s.apellidosCliente(), 100, "Apellidos");
        validarTexto(s.correoCliente(), 100, "Correo");
        validarTexto(s.calle(), 300, "Calle");
        validarTexto(s.region(), 80, "Region");
        validarTexto(s.comuna(), 80, "Comuna");
        validarTexto(s.metodoPago(), 30, "Metodo de pago");

        validarOpcional(s.departamento(), 50, "Departamento");
        validarOpcional(s.indicaciones(), 500, "Indicaciones");

        if (!s.correoCliente().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Correo electronico invalido"
            );
        }

        if (!"retiro".equals(s.tipoEntrega()) &&
                !"domicilio".equals(s.tipoEntrega())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de entrega invalido"
            );
        }

        if (s.pagoAprobado() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes indicar el resultado del pago simulado"
            );
        }

        if (s.productos() == null ||
                s.productos().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La compra debe incluir productos"
            );
        }
    }

    private void validarTexto(
            String valor, int maximo, String campo) {

        if (valor == null || valor.isBlank() ||
                valor.trim().length() > maximo) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Campo invalido: " + campo
            );
        }
    }

    private void validarOpcional(
            String valor, int maximo, String campo) {

        if (valor != null && valor.length() > maximo) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Campo demasiado largo: " + campo
            );
        }
    }
}
