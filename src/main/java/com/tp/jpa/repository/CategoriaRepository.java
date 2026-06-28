package com.tp.jpa.repository;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class CategoriaRepository extends BaseRepository<Categoria> {

    public CategoriaRepository() {
        super(Categoria.class);
    }

    /**
     * Finds all active products belonging to a category by navigating
     * the unidirectional @OneToMany relationship from Categoria to Producto.
     */
    public List<Producto> buscarProductosPorCategoria(Long categoriaId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Producto> query = em.createQuery(
                "SELECT p FROM Categoria c JOIN c.productos p " +
                "WHERE c.id = :categoriaId AND p.eliminado = false",
                Producto.class
            );
            query.setParameter("categoriaId", categoriaId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
