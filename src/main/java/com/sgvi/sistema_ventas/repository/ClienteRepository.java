package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository para la entidad Cliente.
 * Soporta la creación, búsqueda y actualización de información de clientes (RF-010).
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    /**
     * Busca un cliente por su número de documento (DNI, RUC, CE).
     * @param numeroDocumento El número de documento a buscar.
     * @return Un Optional que contiene el Cliente si existe.
     */
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    /**
     * Verifica si existe un cliente con un número de documento específico.
     * @param numeroDocumento El número de documento a verificar (RF-010 Validaciones).
     * @return true si el documento ya existe, false en caso contrario.
     */
    boolean existsByNumeroDocumento(String numeroDocumento);
}