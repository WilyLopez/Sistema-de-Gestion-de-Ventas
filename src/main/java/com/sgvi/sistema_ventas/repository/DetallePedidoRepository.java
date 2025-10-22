package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para la entidad DetallePedido.
 * Proporciona métodos para gestionar detalles de pedido, permitiendo la búsqueda
 * por el pedido al que pertenecen o por el producto que incluyen.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    /**
     * Busca todos los detalles de pedido asociados a un ID de Pedido específico.
     * @param idPedido El ID del Pedido.
     * @return Una lista de DetallePedido que pertenecen al Pedido.
     */
    List<DetallePedido> findByPedidoIdPedido(Long idPedido);

    /**
     * Busca todos los detalles de pedido que contienen un ID de Producto específico.
     * @param idProducto El ID del Producto.
     * @return Una lista de DetallePedido que contienen el Producto.
     */
    List<DetallePedido> findByProductoIdProducto(Long idProducto);
}