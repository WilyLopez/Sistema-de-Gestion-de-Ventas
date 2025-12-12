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
            log.info("Consultando DNI con peruapi.com (nuevo método): {}", numero);

            if (!numero.matches("\\d{8}")) {
                return ResponseEntity.badRequest().body(Map.of("error", "DNI debe tener 8 dígitos"));
            }

            HttpHeaders headers = new HttpHeaders();
            // Usando el encabezado y token correctos según el ejemplo
            headers.set("X-API-KEY", peruApiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // URL y método correctos según el ejemplo
            String url = "https://peruapi.com/api/dni/" + numero;

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET, // El método es GET
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                // Adaptar a la estructura de respuesta de peruapi.com
                Map<String, Object> resultado = new HashMap<>();
                resultado.put("tipoDocumento", "DNI");
                resultado.put("numeroDocumento", responseBody.get("dni"));
                resultado.put("nombres", responseBody.get("nombres"));
                resultado.put("apellidoPaterno", responseBody.get("apellido_paterno"));
                resultado.put("apellidoMaterno", responseBody.get("apellido_materno"));
                resultado.put("nombreCompleto", responseBody.get("nombre_completo"));

                return ResponseEntity.ok(resultado);
            }

            return ResponseEntity.status(response.getStatusCode()).body(Map.of("error", "Error al consultar DNI en peruapi.com."));

        } catch (Exception e) {
            log.error("Error consultando DNI con peruapi.com: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "DNI no encontrado o servicio no disponible en peruapi.com."));
        }
    }

    /**
     * Consulta RUC en SUNAT
     * GET /api/consultas/ruc/{numero}
     */
    @GetMapping("/ruc/{numero}")
    public ResponseEntity<?> consultarRUC(@PathVariable String numero) {
        try {
            log.info("Consultando RUC con peruapi.com (nuevo método): {}", numero);

            if (!numero.matches("\\d{11}")) {
                return ResponseEntity.badRequest().body(Map.of("error", "RUC debe tener 11 dígitos"));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-API-KEY", peruApiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = "https://peruapi.com/api/ruc/" + numero;

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                Map<String, Object> resultado = new HashMap<>();
                resultado.put("tipoDocumento", "RUC");
                resultado.put("numeroDocumento", responseBody.get("ruc"));
                resultado.put("razonSocial", responseBody.get("razon_social"));
                resultado.put("nombreComercial", responseBody.get("nombre_comercial"));
                resultado.put("direccion", responseBody.get("direccion"));
                resultado.put("estado", responseBody.get("estado"));
                resultado.put("condicion", responseBody.get("condicion"));

                return ResponseEntity.ok(resultado);
            }

            return ResponseEntity.status(response.getStatusCode()).body(Map.of("error", "Error al consultar RUC en peruapi.com."));

        } catch (Exception e) {
            log.error("Error consultando RUC con peruapi.com: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "RUC no encontrado o servicio no disponible en peruapi.com."));
        }
    }
}