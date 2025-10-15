package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para la entidad DetallePedido.
 * Proporciona métodos para gestionar detalles de pedido
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    // Buscar detalles por pedido
    List<DetallePedido> findByPedidoId(Long idPedido);

    // Buscar detalles por producto
    List<DetallePedido> findByProductoId(Long idProducto);
}