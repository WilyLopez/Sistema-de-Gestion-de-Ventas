package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.model.entity.Cliente;
import com.sgvi.sistema_ventas.model.enums.TipoDocumento;
import com.sgvi.sistema_ventas.service.interfaces.IClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para gestión de clientes.
 * RF-010: Gestión de Clientes
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clientes", description = "Endpoints para gestión de clientes")
public class ClienteRestController {

    private final IClienteService clienteService;

    /**
     * RF-010: Crear cliente
     * POST /api/clientes
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Crear cliente", description = "Registra un nuevo cliente")
    public ResponseEntity<Cliente> crear(@Valid @RequestBody Cliente cliente) {
        log.info("POST /api/clientes - Crear cliente");
        Cliente clienteCreado = clienteService.crear(cliente);
        return new ResponseEntity<>(clienteCreado, HttpStatus.CREATED);
    }

    /**
     * RF-010: Actualizar cliente
     * PUT /api/clientes/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Actualizar cliente", description = "Actualiza datos de un cliente")
    public ResponseEntity<Cliente> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Cliente cliente) {
        log.info("PUT /api/clientes/{}", id);
        Cliente clienteActualizado = clienteService.actualizar(id, cliente);
        return ResponseEntity.ok(clienteActualizado);
    }

    /**
     * RF-010: Eliminar cliente
     * DELETE /api/clientes/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar cliente", description = "Desactiva un cliente")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/clientes/{}", id);
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * RF-010: Obtener cliente por ID
     * GET /api/clientes/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente", description = "Obtiene un cliente por ID")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/clientes/{}", id);
        Cliente cliente = clienteService.obtenerPorId(id);
        return ResponseEntity.ok(cliente);
    }

    /**
     * RF-010: Listar clientes
     * GET /api/clientes?page=0&size=20
     */
    @GetMapping
    @Operation(summary = "Listar clientes", description = "Lista todos los clientes con paginación")
    public ResponseEntity<Page<Cliente>> listarTodos(Pageable pageable) {
        log.info("GET /api/clientes");
        Page<Cliente> clientes = clienteService.listarTodos(pageable);
        return ResponseEntity.ok(clientes);
    }

    /**
     * RF-010: Buscar cliente por documento
     * GET /api/clientes/documento?tipo=DNI&numero=12345678
     */
    @GetMapping("/documento")
    @Operation(summary = "Buscar por documento", description = "Busca cliente por tipo y número de documento")
    public ResponseEntity<Cliente> buscarPorDocumento(
            @RequestParam TipoDocumento tipo,
            @RequestParam String numero) {
        log.info("GET /api/clientes/documento?tipo={}&numero={}", tipo, numero);
        Cliente cliente = clienteService.buscarPorDocumento(tipo, numero);
        return ResponseEntity.ok(cliente);
    }

    /**
     * RF-010: Buscar clientes por nombre
     * GET /api/clientes/buscar?nombre=Juan
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar por nombre", description = "Busca clientes por nombre o apellido")
    public ResponseEntity<Page<Cliente>> buscarPorNombre(
            @RequestParam String nombre,
            Pageable pageable) {
        log.info("GET /api/clientes/buscar?nombre={}", nombre);
        Page<Cliente> clientes = clienteService.buscarPorNombre(nombre, pageable);
        return ResponseEntity.ok(clientes);
    }
}
