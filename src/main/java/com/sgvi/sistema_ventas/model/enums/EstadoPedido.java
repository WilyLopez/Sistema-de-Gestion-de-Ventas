package com.sgvi.sistema_ventas.model.enums;

/**
 * Estados de pedidos de clientes y reabastecimiento
 * Corresponde a la columna Estado en Pedido y PedidoReabastecimiento
 */
public enum EstadoPedido {
    PENDIENTE("pendiente", "Pendiente", false, false),
    CONFIRMADO("confirmado", "Confirmado", false, false),
    PREPARANDO("preparando", "Preparando", false, false),
    ENVIADO("enviado", "Enviado", false, false),
    ENTREGADO("entregado", "Entregado", true, false),
    CANCELADO("cancelado", "Cancelado", true, true),

    // Específicos para reabastecimiento
    APROBADO("aprobado", "Aprobado", false, false),
    ORDENADO("ordenado", "Ordenado", false, false),
    RECIBIDO_PARCIAL("recibido_parcial", "Recibido Parcial", false, false),
    RECIBIDO_COMPLETO("recibido_completo", "Recibido Completo", true, false);

    private final String valor;
    private final String descripcion;
    private final boolean esFinal; // Estado terminal
    private final boolean esCancelacion;

    EstadoPedido(String valor, String descripcion, boolean esFinal, boolean esCancelacion) {
        this.valor = valor;
        this.descripcion = descripcion;
        this.esFinal = esFinal;
        this.esCancelacion = esCancelacion;
    }

    public String getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEsFinal() {
        return esFinal;
    }

    public boolean isEsCancelacion() {
        return esCancelacion;
    }

    public static EstadoPedido fromValor(String valor) {
        for (EstadoPedido estado : EstadoPedido.values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de pedido no válido: " + valor);
    }

    /**
     * Verifica si el pedido puede ser cancelado
     */
    public boolean puedeCancelarse() {
        return !esFinal && !esCancelacion;
    }

    /**
     * Verifica si el pedido puede ser modificado
     */
    public boolean puedeModificarse() {
        return this == PENDIENTE;
    }

    /**
     * Obtiene la clase CSS para badge
     */
    public String getBadgeClass() {
        return switch (this) {
            case PENDIENTE -> "badge-secondary";
            case CONFIRMADO, APROBADO -> "badge-info";
            case PREPARANDO, ORDENADO -> "badge-warning";
            case ENVIADO -> "badge-primary";
            case ENTREGADO, RECIBIDO_COMPLETO -> "badge-success";
            case CANCELADO -> "badge-danger";
            case RECIBIDO_PARCIAL -> "badge-warning";
        };
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
