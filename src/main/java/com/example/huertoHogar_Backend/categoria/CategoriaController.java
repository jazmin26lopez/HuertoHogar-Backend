
package com.example.huertoHogar_Backend.categoria;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    // GET: listar categorias
    @GetMapping
    public List<CategoriaModel> listarCategorias() {
        return service.listarCategorias();
    }

    // GET: buscar categoria por ID
    @GetMapping("/{id}")
    public CategoriaModel buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // POST: crear categoria
    @PostMapping
    public ResponseEntity<CategoriaModel> crearCategoria(
            @RequestBody CategoriaModel categoria) {

        CategoriaModel nueva = service.crearCategoria(categoria);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nueva);
    }

    // PUT: actualizar categoria
    @PutMapping("/{id}")
    public CategoriaModel actualizarCategoria(
            @PathVariable Long id,
            @RequestBody CategoriaModel categoria) {

        return service.actualizarCategoria(id, categoria);
    }

    // DELETE: eliminar categoria
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(
            @PathVariable Long id) {

        service.eliminarCategoria(id);

        return ResponseEntity.noContent().build();
    }
}
