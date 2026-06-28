package com.tp.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Producto extends Base {

    private String nombre;
    private Double precio;
    private String descripcion;
    private int stock;
    private String imagen;
    private Boolean disponible;

    public Producto(String nombre, Double precio, String descripcion,
                    int stock, String imagen, Boolean disponible) {
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.stock = stock;
        this.imagen = imagen;
        this.disponible = disponible;
    }
}
