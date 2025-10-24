package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.model.entity.Cliente;
import com.sgvi.sistema_ventas.model.enums.TipoDocumento;
import com.sgvi.sistema_ventas.service.interfaces.IClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de clientes.
 * Implementa requisito RF-010 del SRS: Gestión de Clientes.
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
     * RF-010: Crear nuevo cliente.
     * Valida documento único y formato según tipo.
     *
     * @param cliente Datos del cliente
     * @return Cliente creado
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Crear cliente",
            description = "Registra un nuevo cliente. Valida documento según tipo (DNI/RUC/CE)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Cliente.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o documento duplicado/inválido",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            )
    })
    public ResponseEntity<?> crear(@Valid @RequestBody Cliente cliente) {
        try {
            log.info("POST /api/clientes - Crear cliente: {} {}",
                    cliente.getNombre(),
                    cliente.getApellido());

            if (!clienteService.validarDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento())) {
                log.warn("Intento de crear cliente con documento inválido: {} {}",
                        cliente.getTipoDocumento(),
                        cliente.getNumeroDocumento());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El documento no es válido para el tipo: " + cliente.getTipoDocumento()));
            }

            if (clienteService.existeDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento())) {
                log.warn("Intento de crear cliente con documento duplicado: {} {}",
                        cliente.getTipoDocumento(),
                        cliente.getNumeroDocumento());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El documento ya está registrado"));
            }

            if (cliente.getCorreo() != null &&
                    !cliente.getCorreo().trim().isEmpty() &&
                    clienteService.existeCorreo(cliente.getCorreo())) {

                log.warn("Intento de crear cliente con correo duplicado: {}", cliente.getCorreo());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El correo ya está registrado: " + cliente.getCorreo()));
            }

            Cliente clienteCreado = clienteService.crear(cliente);

            log.info("Cliente creado exitosamente: {} {} - ID: {}",
                    clienteCreado.getNombre(),
                    clienteCreado.getApellido(),
                    clienteCreado.getIdCliente());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(clienteCreado);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al crear cliente: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al crear cliente: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al crear cliente. Intente nuevamente"));
        }
    }

    /**
     * RF-010: Actualizar cliente existente.
     * Valida documento si se modifica.
     *
     * @param id ID del cliente
     * @param cliente Datos actualizados
     * @return Cliente actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Actualizar cliente",
            description = "Actualiza datos de un cliente existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente actualizado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o documento duplicado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado"
            )
    })
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Cliente cliente) {

        try {
            log.info("PUT /api/clientes/{} - Actualizar cliente", id);

            Cliente clienteExistente = clienteService.obtenerPorId(id);

            if (!clienteExistente.getNumeroDocumento().equals(cliente.getNumeroDocumento())) {

                if (!clienteService.validarDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento())) {
                    log.warn("Intento de actualizar con documento inválido: {} {}",
                            cliente.getTipoDocumento(),
                            cliente.getNumeroDocumento());
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("El documento no es válido"));
                }

                if (clienteService.existeDocumento(cliente.getTipoDocumento(), cliente.getNumeroDocumento())) {
                    log.warn("Intento de actualizar con documento duplicado");
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("El documento ya está registrado"));
                }
            }

            if (cliente.getCorreo() != null &&
                    !cliente.getCorreo().equals(clienteExistente.getCorreo())) {

                if (clienteService.existeCorreo(cliente.getCorreo())) {
                    log.warn("Intento de actualizar con correo duplicado: {}", cliente.getCorreo());
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("El correo ya está registrado"));
                }
            }

            Cliente clienteActualizado = clienteService.actualizar(id, cliente);

            log.info("Cliente actualizado exitosamente: {}", id);

            return ResponseEntity.ok(clienteActualizado);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al actualizar cliente {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al actualizar cliente {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Cliente no encontrado con ID: " + id));
        }
    }

    /**
     * RF-010: Eliminar cliente (soft delete).
     * Marca el cliente como inactivo.
     *
     * @param id ID del cliente
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Eliminar cliente",
            description = "Desactiva un cliente del sistema (soft delete). Solo administrador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Cliente eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (solo administrador)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado"
            )
    })
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            log.info("DELETE /api/clientes/{} - Eliminar cliente", id);

            clienteService.eliminar(id);

            log.info("Cliente eliminado exitosamente: {}", id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error al eliminar cliente {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Cliente no encontrado con ID: " + id));
        }
    }

    /**
     * RF-010: Obtener cliente por ID.
     *
     * @param id ID del cliente
     * @return Cliente encontrado
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener cliente por ID",
            description = "Obtiene los datos completos de un cliente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado"
            )
    })
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            log.info("GET /api/clientes/{}", id);
            Cliente cliente = clienteService.obtenerPorId(id);
            return ResponseEntity.ok(cliente);

        } catch (Exception e) {
            log.error("Error al obtener cliente {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Cliente no encontrado con ID: " + id));
        }
    }

    /**
     * RF-010: Buscar cliente por tipo y número de documento.
     *
     * @param tipo Tipo de documento (DNI, RUC, CE)
     * @param numero Número del documento
     * @return Cliente encontrado
     */
    @GetMapping("/documento")
    @Operation(
            summary = "Buscar por documento",
            description = "Busca un cliente por tipo y número de documento. Ejemplo: ?tipo=DNI&numero=12345678"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado con ese documento"
            )
    })
    public ResponseEntity<?> buscarPorDocumento(
            @RequestParam TipoDocumento tipo,
            @RequestParam String numero) {

        try {
            log.info("GET /api/clientes/documento?tipo={}&numero={}", tipo, numero);
            Cliente cliente = clienteService.buscarPorDocumento(tipo, numero);
            return ResponseEntity.ok(cliente);

        } catch (Exception e) {
            log.error("Error al buscar cliente por documento {} {}: {}", tipo, numero, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Cliente no encontrado con documento: " + tipo + " " + numero));
        }
    }

    /**
     * RF-010: Listar todos los clientes con paginación.
     *
     * @param pageable Parámetros de paginación
     * @return Página de clientes
     */
    @GetMapping
    @Operation(
            summary = "Listar clientes",
            description = "Lista todos los clientes con paginación. Ejemplo: ?page=0&size=20&sort=nombre,asc"
    )
    public ResponseEntity<Page<Cliente>> listarTodos(Pageable pageable) {
        log.info("GET /api/clientes - Listar clientes (página: {})", pageable.getPageNumber());
        Page<Cliente> clientes = clienteService.listarTodos(pageable);
        return ResponseEntity.ok(clientes);
    }

    /**
     * RF-010: Listar solo clientes activos con paginación.
     *
     * @param pageable Parámetros de paginación
     * @return Página de clientes activos
     */
    @GetMapping("/activos")
    @Operation(
            summary = "Clientes activos",
            description = "Lista solo clientes activos con paginación"
    )
    public ResponseEntity<Page<Cliente>> listarActivos(Pageable pageable) {
        log.info("GET /api/clientes/activos");
        Page<Cliente> clientes = clienteService.listarActivos(pageable);
        return ResponseEntity.ok(clientes);
    }

    /**
     * RF-010: Buscar clientes por nombre o apellido.
     *
     * @param nombre Texto a buscar
     * @param pageable Parámetros de paginación
     * @return Página de clientes que coinciden
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar por nombre",
            description = "Busca clientes por nombre o apellido (búsqueda case-insensitive)"
    )
    public ResponseEntity<Page<Cliente>> buscarPorNombre(
            @RequestParam String nombre,
            Pageable pageable) {

        log.info("GET /api/clientes/buscar?nombre={}", nombre);
        Page<Cliente> clientes = clienteService.buscarPorNombre(nombre, pageable);
        return ResponseEntity.ok(clientes);
    }

    /**
     * Validar documento según tipo.
     * Verifica formato y dígito verificador.
     *
     * @param tipo Tipo de documento
     * @param numero Número del documento
     * @return Objeto con resultado de validación
     */
    @GetMapping("/validar-documento")
    @Operation(
            summary = "Validar documento",
            description = "Valida el formato de un documento según su tipo y verifica si ya está registrado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Validación completada"
            )
    })
    public ResponseEntity<Map<String, Object>> validarDocumento(
            @RequestParam TipoDocumento tipo,
            @RequestParam String numero) {

        try {
            log.info("GET /api/clientes/validar-documento?tipo={}&numero={}", tipo, numero);

            boolean esValido = clienteService.validarDocumento(tipo, numero);
            boolean existe = false;

            if (esValido) {
                existe = clienteService.existeDocumento(tipo, numero);
            }

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("tipoDocumento", tipo);
            respuesta.put("numeroDocumento", numero);
            respuesta.put("esValido", esValido);
            respuesta.put("existe", existe);
            respuesta.put("puedeRegistrar", esValido && !existe);

            if (!esValido) {
                respuesta.put("mensaje", "Documento inválido. Verifique el formato");
            } else if (existe) {
                respuesta.put("mensaje", "Documento válido pero ya está registrado");
            } else {
                respuesta.put("mensaje", "Documento válido y disponible para registro");
            }

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            log.error("Error al validar documento {} {}: {}", tipo, numero, e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "tipoDocumento", tipo,
                    "numeroDocumento", numero,
                    "esValido", false,
                    "existe", false,
                    "puedeRegistrar", false,
                    "mensaje", "Error al validar documento"
            ));
        }
    }

    /**
     * Verificar si un documento ya está registrado.
     * Útil para validación en tiempo real en formularios.
     *
     * @param tipo Tipo de documento
     * @param numero Número del documento
     * @return Objeto con booleano indicando si existe
     */
    @GetMapping("/existe-documento")
    @Operation(
            summary = "Verificar existencia de documento",
            description = "Verifica si un documento ya está registrado en el sistema"
    )
    public ResponseEntity<Map<String, Boolean>> existeDocumento(
            @RequestParam TipoDocumento tipo,
            @RequestParam String numero) {

        log.info("GET /api/clientes/existe-documento?tipo={}&numero={}", tipo, numero);
        boolean existe = clienteService.existeDocumento(tipo, numero);
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    /**
     * Verificar si un correo ya está registrado.
     * Útil para validación en tiempo real en formularios.
     *
     * @param correo Correo a verificar
     * @return Objeto con booleano indicando si existe
     */
    @GetMapping("/existe-correo")
    @Operation(
            summary = "Verificar existencia de correo",
            description = "Verifica si un correo electrónico ya está registrado"
    )
    public ResponseEntity<Map<String, Boolean>> existeCorreo(@RequestParam String correo) {
        log.info("GET /api/clientes/existe-correo?correo={}", correo);
        boolean existe = clienteService.existeCorreo(correo);
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    /**
     * Obtener estadísticas de clientes.
     * Solo administrador.
     *
     * @return Estadísticas (total, activos, inactivos)
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Estadísticas de clientes",
            description = "Obtiene estadísticas generales de clientes"
    )
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        log.info("GET /api/clientes/estadisticas");

        Page<Cliente> todos = clienteService.listarTodos(Pageable.unpaged());
        Page<Cliente> activos = clienteService.listarActivos(Pageable.unpaged());

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalClientes", todos.getTotalElements());
        estadisticas.put("clientesActivos", activos.getTotalElements());
        estadisticas.put("clientesInactivos", todos.getTotalElements() - activos.getTotalElements());

        return ResponseEntity.ok(estadisticas);
    }
}
