
package com.example.huertoHogar_Backend.boleta;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
public class BoletaController {

    private final BoletaService service;

    public BoletaController(BoletaService service) {
        this.service = service;
    }

    // POST: registrar compra
    @PostMapping
    public ResponseEntity<BoletaService.RespuestaBoleta> crear(
            @RequestBody BoletaService.SolicitudCompra solicitud) {

        BoletaService.RespuestaBoleta respuesta =
                service.crearBoleta(solicitud);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    // GET: listar boletas con sus detalles
    @GetMapping
    public List<BoletaService.RespuestaBoleta> listar() {
        return service.listarBoletas();
    }

    // GET: consultar boleta por ID
    @GetMapping("/{id}")
    public BoletaService.RespuestaBoleta buscar(
            @PathVariable Long id) {

        return service.buscarPorId(id);
    }
}
