package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.entity.MetodoPago;
import com.sgvi.sistema_ventas.repository.MetodoPagoRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metodos-pago")
@RequiredArgsConstructor
@Tag(name = "Metodos de Pago", description = "Endpoints para gestión de metodos de pago")
public class MetodoPagoRestController {

    private final MetodoPagoRepository metodoPagoRepository;

    @GetMapping
    public ResponseEntity<List<MetodoPago>> getAll() {
        return ResponseEntity.ok(metodoPagoRepository.findAll());
    }
}
