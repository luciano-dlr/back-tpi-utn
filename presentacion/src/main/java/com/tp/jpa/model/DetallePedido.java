package com.tp.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "detalles_pedido")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"pedido", "producto"})
public class DetallePedido extends Base {

    private int cantidad;
    private Double subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    public DetallePedido(int cantidad, Pedido pedido, Producto producto) {
        this.cantidad = cantidad;
        this.pedido = pedido;
        this.producto = producto;
        this.subtotal = cantidad * producto.getPrecio();
    }
}
