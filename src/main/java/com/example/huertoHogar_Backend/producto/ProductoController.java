
package com.example.huertoHogar_Backend.producto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(
        name = "Productos",
        description = "API para consultar y administrar los productos de HuertoHogar"
)
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar productos",
            description = "Obtiene el catálogo de productos activos. Acceso público."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado obtenido correctamente"
    )
    @GetMapping
    public List<ProductoModel> listar() {
        return service.listarProductos();
    }

    @Operation(
            summary = "Buscar producto por ID",
            description = "Consulta la información de un producto. Acceso público."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ProductoModel buscar(
            @Parameter(description = "ID del producto", example = "1")
            @PathVariable Long id) {

        return service.buscarPorId(id);
    }

    @Operation(
            summary = "Listar productos por categoría",
            description = "Obtiene los productos activos de una categoría. Acceso público."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de productos de la categoría"
    )
    @GetMapping("/categoria/{id}")
    public List<ProductoModel> porCategoria(
            @Parameter(description = "ID de la categoría", example = "1")
            @PathVariable Long id) {

        return service.listarPorCategoria(id);
    }

    @Operation(
            summary = "Crear producto",
            description = "Registra un producto nuevo. Requiere rol Administrador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto creado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Permisos insuficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Código de producto duplicado",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<ProductoModel> crear(
            @RequestBody ProductoModel datos) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crearProducto(datos));
    }

    @Operation(
            summary = "Actualizar producto",
            description = "Modifica un producto existente. Requiere rol Administrador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto actualizado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Permisos insuficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto o categoría no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Código de producto duplicado",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    public ProductoModel actualizar(
            @Parameter(description = "ID del producto", example = "1")
            @PathVariable Long id,
            @RequestBody ProductoModel datos) {

        return service.actualizarProducto(id, datos);
    }

    @Operation(
            summary = "Desactivar producto",
            description = "Realiza una eliminación lógica del producto. Requiere rol Administrador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Producto desactivado correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Permisos insuficientes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del producto", example = "1")
            @PathVariable Long id) {

        service.desactivarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
