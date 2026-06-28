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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"usuario", "detalles"})
public class Pedido extends Base implements Calculable {

    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    private Double total;

    @Enumerated(EnumType.STRING)
    private FormaPago formaPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido(LocalDate fecha, Estado estado, FormaPago formaPago, Usuario usuario) {
        this.fecha = fecha;
        this.estado = estado;
        this.formaPago = formaPago;
        this.usuario = usuario;
    }

    public void addDetallePedido(int cantidad, Producto producto) {
        DetallePedido detalle = new DetallePedido(cantidad, this, producto);
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
