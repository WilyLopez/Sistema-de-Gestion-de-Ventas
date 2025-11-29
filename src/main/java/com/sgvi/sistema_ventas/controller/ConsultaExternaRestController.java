package com.sgvi.sistema_ventas.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ConsultaExternaRestController {

    @Value("${peru.api.url:https://apiperu.dev/api}")
    private String peruApiUrl;

    @Value("${peru.api.token}")
    private String peruApiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Consulta DNI en RENIEC
     * GET /api/consultas/dni/{numero}
     */
    @GetMapping("/dni/{numero}")
    public ResponseEntity<?> consultarDNI(@PathVariable String numero) {
        try {
            log.info("Consultando DNI: {}", numero);

            // Validar formato DNI (8 dígitos)
            if (!numero.matches("\\d{8}")) {
                return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "DNI debe tener 8 dígitos"));
            }

            // Configurar headers con el token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + peruApiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Hacer la petición a la API externa
            String url = peruApiUrl + "/dni/" + numero;
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> data = response.getBody();
                
                // Transformar respuesta a formato consistente
                Map<String, Object> resultado = new HashMap<>();
                resultado.put("tipoDocumento", "DNI");
                resultado.put("numeroDocumento", numero);
                resultado.put("nombres", data.get("nombres"));
                resultado.put("apellidoPaterno", data.get("apellido_paterno"));
                resultado.put("apellidoMaterno", data.get("apellido_materno"));
                resultado.put("nombreCompleto", 
                    data.get("nombres") + " " + 
                    data.get("apellido_paterno") + " " + 
                    data.get("apellido_materno")
                );

                return ResponseEntity.ok(resultado);
            }

            return ResponseEntity
                .status(response.getStatusCode())
                .body(Map.of("error", "Error al consultar DNI"));

        } catch (Exception e) {
            log.error("Error consultando DNI: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "DNI no encontrado o servicio no disponible"));
        }
    }

    /**
     * Consulta RUC en SUNAT
     * GET /api/consultas/ruc/{numero}
     */
    @GetMapping("/ruc/{numero}")
    public ResponseEntity<?> consultarRUC(@PathVariable String numero) {
        try {
            log.info("Consultando RUC: {}", numero);

            // Validar formato RUC (11 dígitos)
            if (!numero.matches("\\d{11}")) {
                return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "RUC debe tener 11 dígitos"));
            }

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + peruApiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Hacer la petición
            String url = peruApiUrl + "/ruc/" + numero;
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> data = response.getBody();
                
                // Transformar respuesta
                Map<String, Object> resultado = new HashMap<>();
                resultado.put("tipoDocumento", "RUC");
                resultado.put("numeroDocumento", numero);
                resultado.put("razonSocial", data.get("razon_social"));
                resultado.put("nombreComercial", data.get("nombre_comercial"));
                resultado.put("direccion", data.get("direccion"));
                resultado.put("estado", data.get("estado"));
                resultado.put("condicion", data.get("condicion"));

                return ResponseEntity.ok(resultado);
            }

            return ResponseEntity
                .status(response.getStatusCode())
                .body(Map.of("error", "Error al consultar RUC"));

        } catch (Exception e) {
            log.error("Error consultando RUC: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "RUC no encontrado o servicio no disponible"));
        }
    }
}