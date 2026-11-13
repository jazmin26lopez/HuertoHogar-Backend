
package com.example.huertoHogar_Backend.producto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductoModel> listar() {
        return service.listarProductos();
    }

    @GetMapping("/{id}")
    public ProductoModel buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/categoria/{id}")
    public List<ProductoModel> porCategoria(@PathVariable Long id) {
        return service.listarPorCategoria(id);
    }

    @PostMapping
    public ResponseEntity<ProductoModel> crear(
            @RequestBody ProductoModel datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crearProducto(datos));
    }

    @PutMapping("/{id}")
    public ProductoModel actualizar(
            @PathVariable Long id,
            @RequestBody ProductoModel datos) {
        return service.actualizarProducto(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.desactivarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
