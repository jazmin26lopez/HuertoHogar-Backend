
package com.example.huertoHogar_Backend.categoria;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(
        name = "Categorías",
        description = "API para consultar y administrar las categorías de productos"
)
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar categorías",
            description = "Obtiene todas las categorías registradas. Acceso público."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado obtenido correctamente"
    )
    @GetMapping
    public List<CategoriaModel> listarCategorias() {
        return service.listarCategorias();
    }

    @Operation(
            summary = "Buscar categoría por ID",
            description = "Obtiene una categoría a partir de su identificador. Acceso público."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/{id}")
    public CategoriaModel buscarPorId(
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long id) {

        return service.buscarPorId(id);
    }

    @Operation(
            summary = "Crear categoría",
            description = "Registra una nueva categoría. Requiere rol Administrador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos"),
            @ApiResponse(responseCode = "409", description = "La categoría ya existe")
    })
    @PostMapping
    public ResponseEntity<CategoriaModel> crearCategoria(
            @RequestBody CategoriaModel categoria) {

        CategoriaModel nueva = service.crearCategoria(categoria);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nueva);
    }

    @Operation(
            summary = "Actualizar categoría",
            description = "Modifica los datos de una categoría existente. Requiere rol Administrador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "409", description = "Nombre de categoría duplicado")
    })
    @PutMapping("/{id}")
    public CategoriaModel actualizarCategoria(
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long id,
            @RequestBody CategoriaModel categoria) {

        return service.actualizarCategoria(id, categoria);
    }

    @Operation(
            summary = "Eliminar categoría",
            description = "Elimina una categoría. Requiere rol Administrador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuario sin permisos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "409", description = "La categoría tiene productos asociados")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long id) {

        service.eliminarCategoria(id);

        return ResponseEntity.noContent().build();
    }
}
