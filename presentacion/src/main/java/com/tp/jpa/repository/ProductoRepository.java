package com.tp.jpa.repository;

import com.tp.jpa.model.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;


public class ProductoRepository extends BaseRepository<Producto> {

    public ProductoRepository() {
        super(Producto.class);
    }


    public List<Producto> buscarPorCategoria(Long categoriaId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Producto> query = em.createQuery(
                "SELECT p FROM Producto p " +
                "WHERE p.categoria.id = :categoriaId " +
                "AND p.eliminado = false",
                Producto.class
            );
            query.setParameter("categoriaId", categoriaId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
