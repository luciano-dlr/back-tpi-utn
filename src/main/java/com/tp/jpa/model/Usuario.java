package com.tp.jpa.model;

import com.tp.jpa.model.enums.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "pedidos")
public class Usuario extends Base {

    private String nombre;
    private String apellido;
    private String mail;
    private String celular;
    private String contrasena;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Set<Pedido> pedidos = new HashSet<>();

    public Usuario(String nombre, String apellido, String mail,
                   String celular, String contrasena, Rol rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.mail = mail;
        this.celular = celular;
        this.contrasena = contrasena;
        this.rol = rol;
    }
}
