package com.tp.jpa.repository;

import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class UsuarioRepository extends BaseRepository<Usuario> {

    public UsuarioRepository() {
        super(Usuario.class);
    }

    /**
     * Finds a user by email, filtering out logically deleted ones.
     * Returns Optional.empty() if no match is found.
     */
    public Optional<Usuario> buscarPorMail(String mail) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.mail = :mail AND u.eliminado = false",
                Usuario.class
            );
            query.setParameter("mail", mail);
            List<Usuario> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Finds all active orders for a user by navigating the
     * unidirectional @OneToMany relationship from Usuario to Pedido.
     */
    public List<Pedido> buscarPedidosPorUsuario(Long idUsuario) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Pedido> query = em.createQuery(
                "SELECT p FROM Usuario u JOIN u.pedidos p " +
                "WHERE u.id = :uid AND p.eliminado = false",
                Pedido.class
            );
            query.setParameter("uid", idUsuario);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
