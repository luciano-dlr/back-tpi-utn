package com.tp.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "productos")
public class Categoria extends Base {

    private String nombre;
    private String descripcion;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Set<Producto> productos = new HashSet<>();

    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
}
