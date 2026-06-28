package com.tp.jpa.model;

import com.tp.jpa.interfaces.Calculable;
import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.model.enums.FormaPago;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "detalles")
public class Pedido extends Base implements Calculable {

    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    private Double total;

    @Enumerated(EnumType.STRING)
    private FormaPago formaPago;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Set<DetallePedido> detalles = new HashSet<>();

    public Pedido(LocalDate fecha, Estado estado, FormaPago formaPago) {
        this.fecha = fecha;
        this.estado = estado;
        this.formaPago = formaPago;
    }

    public void addDetallePedido(int cantidad, Producto producto) {
        DetallePedido detalle = new DetallePedido(cantidad, producto);
        this.detalles.add(detalle);
    }

    public DetallePedido findeDetallePedidoByProducto(Producto producto) {
        return detalles.stream()
                .filter(d -> d.getProducto().getId().equals(producto.getId()))
                .findFirst().orElse(null);
    }

    public void deleteDetallePedidoByProducto(Producto producto) {
        detalles.removeIf(d -> d.getProducto().getId().equals(producto.getId()));
    }

    @Override
    public void calcularTotal() {
        this.total = detalles.stream().mapToDouble(DetallePedido::getSubtotal).sum();
    }
}
