
package com.example.huertoHogar_Backend.boleta;

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
@RequestMapping("/api/v1/boletas")
@Tag(
        name = "Boletas",
        description = "API para registrar compras y consultar boletas de HuertoHogar"
)
public class BoletaController {

    private final BoletaService service;

    public BoletaController(BoletaService service) {
        this.service = service;
    }

    @Operation(
            summary = "Registrar compra",
            description = "Registra una compra asociada al usuario autenticado. " +
                    "Pueden comprar el Cliente y el Administrador; el Vendedor no. " +
                    "Calcula subtotales, descuentos y envío. " +
                    "Descuenta stock únicamente si el pago simulado fue aprobado. " +
                    "Un pago rechazado se registra con estado FALLIDA."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Compra registrada con estado PAGADA o FALLIDA"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de compra inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El rol del usuario (Vendedor) no puede comprar",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Stock insuficiente",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<BoletaService.RespuestaBoleta> crear(
            @RequestBody BoletaService.SolicitudCompra solicitud) {

        BoletaService.RespuestaBoleta respuesta =
                service.crearBoleta(solicitud);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @Operation(
            summary = "Listar boletas",
            description = "Obtiene todas las boletas registradas con sus detalles. " +
                    "Disponible para Administrador y Vendedor."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de boletas obtenido correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario sin permisos",
                    content = @Content
            )
    })
    @GetMapping
    public List<BoletaService.RespuestaBoleta> listar() {
        return service.listarBoletas();
    }

    @Operation(
            summary = "Consultar boleta por ID",
            description = "Obtiene una boleta y los productos asociados. " +
                    "Disponible para Administrador y Vendedor."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Boleta encontrada"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario sin permisos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Boleta no encontrada",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public BoletaService.RespuestaBoleta buscar(
            @Parameter(
                    description = "Identificador de la boleta",
                    example = "1"
            )
            @PathVariable Long id) {

        return service.buscarPorId(id);
    }
}
