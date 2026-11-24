
package com.example.huertoHogar_Backend.producto;

import com.example.huertoHogar_Backend.categoria.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository repository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(
            ProductoRepository repository,
            CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<ProductoModel> listarProductos() {
        return repository.findByActivo("S");
    }

    public ProductoModel buscarPorId(Long id) {
        ProductoModel producto = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (!"S".equals(producto.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Producto no disponible");
        }

        return producto;
    }

    public List<ProductoModel> listarPorCategoria(Long categoriaId) {
        return repository.findByCategoriaIdAndActivo(
                categoriaId, "S");
    }

    @Transactional
    public ProductoModel crearProducto(ProductoModel datos) {
        validarProducto(datos);

        String codigo = datos.getCodigo().trim();

        if (repository.existsByCodigoIgnoreCase(codigo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El codigo ya existe");
        }

        datos.setProductoId(null);
        datos.setCodigo(codigo);
        datos.setNombre(datos.getNombre().trim());
        datos.setActivo("S");

        if (datos.getDescuentoOferta() == null) {
            datos.setDescuentoOferta(0);
        }

        return repository.saveAndFlush(datos);
    }

    @Transactional
    public ProductoModel actualizarProducto(
            Long id, ProductoModel datos) {

        ProductoModel producto = buscarPorId(id);
        validarProducto(datos);

        String codigo = datos.getCodigo().trim();

        if (repository.existsByCodigoIgnoreCaseAndProductoIdNot(
                codigo, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El codigo ya existe");
        }

        producto.setCodigo(codigo);
        producto.setNombre(datos.getNombre().trim());
        producto.setCategoriaId(datos.getCategoriaId());
        producto.setPrecio(datos.getPrecio());
        producto.setStock(datos.getStock());
        producto.setUnidadMedida(datos.getUnidadMedida());
        producto.setOrigen(datos.getOrigen());
        producto.setDescripcion(datos.getDescripcion());
        producto.setImagen(datos.getImagen());
        producto.setDescuentoOferta(
                datos.getDescuentoOferta() == null
                        ? 0 : datos.getDescuentoOferta());

        return repository.saveAndFlush(producto);
    }

    @Transactional
    public void desactivarProducto(Long id) {
        ProductoModel producto = buscarPorId(id);
        producto.setActivo("N");
        repository.saveAndFlush(producto);
    }

    private void validarProducto(ProductoModel producto) {

        if (producto == null ||
                producto.getCodigo() == null ||
                producto.getCodigo().isBlank() ||
                producto.getCodigo().trim().length() > 10 ||
                producto.getNombre() == null ||
                producto.getNombre().isBlank() ||
                producto.getNombre().trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Codigo o nombre invalido");
        }

        if (producto.getCategoriaId() == null ||
                !categoriaRepository.existsById(
                        producto.getCategoriaId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Categoria inexistente");
        }

        if (producto.getPrecio() == null ||
                producto.getPrecio().compareTo(BigDecimal.ZERO) < 0 ||
                producto.getPrecio().precision() > 10 ||
                producto.getPrecio().scale() > 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Precio invalido");
        }

        if (producto.getStock() == null ||
                producto.getStock() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stock invalido");
        }

        int descuento = producto.getDescuentoOferta() == null
                ? 0 : producto.getDescuentoOferta();

        if (descuento < 0 || descuento > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Descuento fuera de rango");
        }

        if ((producto.getUnidadMedida() != null &&
                producto.getUnidadMedida().length() > 20) ||
                (producto.getOrigen() != null &&
                        producto.getOrigen().length() > 100) ||
                (producto.getDescripcion() != null &&
                        producto.getDescripcion().length() > 1000) ||
                (producto.getImagen() != null &&
                        producto.getImagen().length() > 200)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Uno de los campos supera su longitud maxima");
        }
    }
}
