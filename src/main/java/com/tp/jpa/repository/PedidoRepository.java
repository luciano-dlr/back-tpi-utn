package com.tp.jpa.repository;

import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.enums.Estado;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class PedidoRepository extends BaseRepository<Pedido> {

    public PedidoRepository() {
        super(Pedido.class);
    }

    /**
     * Finds all active orders with the given status.
     */
    public List<Pedido> buscarPorEstado(Estado estado) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Pedido> query = em.createQuery(
                "SELECT p FROM Pedido p WHERE p.estado = :estado AND p.eliminado = false",
                Pedido.class
            );
            query.setParameter("estado", estado);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
