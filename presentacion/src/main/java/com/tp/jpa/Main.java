package com.tp.jpa;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.Producto;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.ProductoRepository;
import com.tp.jpa.util.JPAUtil;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final CategoriaRepository categoriaRepo = new CategoriaRepository();
    private static final ProductoRepository  productoRepo  = new ProductoRepository();

    public static void main(String[] args) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║    SISTEMA DE GESTIÓN JPA    ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║  1. Categorías               ║");
            System.out.println("║  2. Productos                ║");
            System.out.println("║  3. Reportes                 ║");
            System.out.println("║  0. Salir                    ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.print("Seleccione: ");
            switch (leerInt()) {
                case 1  -> menuCategorias();
                case 2  -> menuProductos();
                case 3  -> menuReportes();
                case 0  -> salir = true;
                default -> System.out.println(" Opción inválida.");
            }
        }
        JPAUtil.close();
        System.out.println("¡Adios!");
    }

    //  SUBMENU CATEGORÍAS

    private static void menuCategorias() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── CATEGORÍAS ──────────────────");
            System.out.println("  1. Alta");
            System.out.println("  2. Baja lógica");
            System.out.println("  3. Modificación");
            System.out.println("  4. Listado");
            System.out.println("  0. Volver");
            System.out.print("Seleccione: ");
            switch (leerInt()) {
                case 1  -> altaCategoria();
                case 2  -> bajaCategoria();
                case 3  -> modificarCategoria();
                case 4  -> listarCategorias();
                case 0  -> volver = true;
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    /** Alta de categoría */
    private static void altaCategoria() {
        System.out.println("\n> Alta de Categoría");

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine().trim();
        if (nombre.isEmpty()) {
            System.out.println("✘ Error: el nombre no puede estar vacío.");
            return;
        }
        System.out.print("Descripción: ");
        String descripcion = scanner.nextLine().trim();

        Categoria cat = new Categoria(nombre, descripcion);
        Categoria guardada = categoriaRepo.guardar(cat);
        System.out.println("Categoría creada. ID generado: " + guardada.getId());
    }

    /** Baja lógica de categoría */
    private static void bajaCategoria() {
        System.out.println("\n> Baja de Categoría");
        listarCategorias();

        System.out.print("ID de la categoría a dar de baja: ");
        Long id = leerLong();
        if (id == null) { System.out.println("✘ ID inválido."); return; }

        Optional<Categoria> opt = categoriaRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("✘ Error: no existe una categoría activa con ese ID.");
            return;
        }
        String nombre = opt.get().getNombre();
        categoriaRepo.eliminarLogico(id);
        System.out.println("✔ Categoría \"" + nombre + "\" dada de baja correctamente.");
    }

    /** HU-04: Modificación de categoría */
    private static void modificarCategoria() {
        System.out.println("\n> Modificar Categoría");
        listarCategorias();

        System.out.print("ID de la categoría a modificar: ");
        Long id = leerLong();
        if (id == null) { System.out.println("✘ ID inválido."); return; }

        Optional<Categoria> opt = categoriaRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("✘ Error: no existe una categoría activa con ese ID.");
            return;
        }

        Categoria cat = opt.get();
        System.out.println("Valores actuales → Nombre: \"" + cat.getNombre()
                         + "\" | Descripción: \"" + cat.getDescripcion() + "\"");
        System.out.println("(Presioná Enter para conservar el valor actual)");

        System.out.print("Nuevo nombre [" + cat.getNombre() + "]: ");
        String nuevoNombre = scanner.nextLine().trim();
        if (!nuevoNombre.isEmpty()) cat.setNombre(nuevoNombre);

        System.out.print("Nueva descripción [" + cat.getDescripcion() + "]: ");
        String nuevaDesc = scanner.nextLine().trim();
        if (!nuevaDesc.isEmpty()) cat.setDescripcion(nuevaDesc);

        categoriaRepo.guardar(cat);
        System.out.println("✔ Categoría actualizada correctamente.");
    }

    /** Muestra todas las categorías activas */
    private static void listarCategorias() {
        List<Categoria> lista = categoriaRepo.listarActivos();
        if (lista.isEmpty()) {
            System.out.println("  (No hay categorías activas)");
            return;
        }
        System.out.println("\n  ID  | Nombre              | Descripción");
        System.out.println("  ────┼─────────────────────┼───────────────────────────");
        for (Categoria c : lista) {
            System.out.printf("  %-4d| %-21s| %s%n",
                    c.getId(), c.getNombre(), c.getDescripcion());
        }
    }

    //  SUBMENU PRODUCTOS

    private static void menuProductos() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── PRODUCTOS ───────────────────");
            System.out.println("  1. Alta");
            System.out.println("  2. Baja lógica");
            System.out.println("  3. Modificación");
            System.out.println("  4. Listado");
            System.out.println("  0. Volver");
            System.out.print("Seleccione: ");
            switch (leerInt()) {
                case 1  -> altaProducto();
                case 2  -> bajaProducto();
                case 3  -> modificarProducto();
                case 4  -> listarProductos();
                case 0  -> volver = true;
                default -> System.out.println("⚠ Opción inválida.");
            }
        }
    }

    /** Alta de producto */
    private static void altaProducto() {
        System.out.println("\n> Alta de Producto");

        // Listar categorías activas para que el usuario elija
        List<Categoria> categorias = categoriaRepo.listarActivos();
        if (categorias.isEmpty()) {
            System.out.println("✘ No hay categorías activas. Cree una categoría primero.");
            return;
        }
        System.out.println("Categorías disponibles:");
        listarCategorias();

        System.out.print("ID de categoría: ");
        Long catId = leerLong();
        if (catId == null) { System.out.println("✘ ID inválido."); return; }

        Optional<Categoria> catOpt = categoriaRepo.buscarPorId(catId);
        if (catOpt.isEmpty() || catOpt.get().isEliminado()) {
            System.out.println("✘ Error: categoría no encontrada.");
            return;
        }

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine().trim();
        if (nombre.isEmpty()) { System.out.println("✘ Error: el nombre no puede estar vacío."); return; }

        System.out.print("Descripción: ");
        String descripcion = scanner.nextLine().trim();

        // Validar precio > 0
        double precio = 0;
        while (precio <= 0) {
            System.out.print("Precio (mayor a 0): ");
            String input = scanner.nextLine().trim();
            try {
                precio = Double.parseDouble(input);
                if (precio <= 0) System.out.println("✘ El precio debe ser mayor a 0.");
            } catch (NumberFormatException e) {
                System.out.println("✘ Valor inválido, ingresá un número.");
            }
        }

        // Validar stock >= 0
        int stock = -1;
        while (stock < 0) {
            System.out.print("Stock (mayor o igual a 0): ");
            String input = scanner.nextLine().trim();
            try {
                stock = Integer.parseInt(input);
                if (stock < 0) System.out.println("✘ El stock no puede ser negativo.");
            } catch (NumberFormatException e) {
                System.out.println("✘ Valor inválido, ingresá un número entero.");
            }
        }

        System.out.print("Imagen (Enter para omitir): ");
        String imagen = scanner.nextLine().trim();

        Producto producto = new Producto(nombre, precio, descripcion, stock, imagen, true, catOpt.get());
        Producto guardado = productoRepo.guardar(producto);
        System.out.println("✔ Producto creado. ID: " + guardado.getId()
                         + " | Categoría: " + catOpt.get().getNombre());
    }

    /** Baja lógica de producto */
    private static void bajaProducto() {
        System.out.println("\n> Baja de Producto");
        listarProductos();

        System.out.print("ID del producto a dar de baja: ");
        Long id = leerLong();
        if (id == null) { System.out.println("✘ ID inválido."); return; }

        Optional<Producto> opt = productoRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("✘ Error: no existe un producto activo con ese ID.");
            return;
        }
        String nombre = opt.get().getNombre();
        productoRepo.eliminarLogico(id);
        System.out.println("✔ Producto \"" + nombre + "\" dado de baja correctamente.");
    }

    /** Modificación de producto */
    private static void modificarProducto() {
        System.out.println("\n> Modificar Producto");
        listarProductos();

        System.out.print("ID del producto a modificar: ");
        Long id = leerLong();
        if (id == null) { System.out.println("✘ ID inválido."); return; }

        Optional<Producto> opt = productoRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("✘ Error: no existe un producto activo con ese ID.");
            return;
        }

        Producto prod = opt.get();
        System.out.println("Valores actuales → Nombre: \"" + prod.getNombre()
                         + "\" | Precio: $" + prod.getPrecio()
                         + " | Stock: " + prod.getStock());
        System.out.println("(Presioná Enter para conservar el valor actual)");

        // Nombre
        System.out.print("Nuevo nombre [" + prod.getNombre() + "]: ");
        String nuevoNombre = scanner.nextLine().trim();
        if (!nuevoNombre.isEmpty()) prod.setNombre(nuevoNombre);

        // Precio con validación
        System.out.print("Nuevo precio [" + prod.getPrecio() + "]: ");
        String precioStr = scanner.nextLine().trim();
        if (!precioStr.isEmpty()) {
            try {
                double nuevoPrecio = Double.parseDouble(precioStr);
                if (nuevoPrecio <= 0) System.out.println("✘ Precio inválido (debe ser > 0). Se mantiene el valor anterior.");
                else prod.setPrecio(nuevoPrecio);
            } catch (NumberFormatException e) {
                System.out.println("✘ Valor no numérico. Se mantiene el precio anterior.");
            }
        }

        // Stock con validación
        System.out.print("Nuevo stock [" + prod.getStock() + "]: ");
        String stockStr = scanner.nextLine().trim();
        if (!stockStr.isEmpty()) {
            try {
                int nuevoStock = Integer.parseInt(stockStr);
                if (nuevoStock < 0) System.out.println("✘ Stock inválido (no puede ser negativo). Se mantiene el valor anterior.");
                else prod.setStock(nuevoStock);
            } catch (NumberFormatException e) {
                System.out.println("✘ Valor no numérico. Se mantiene el stock anterior.");
            }
        }

        productoRepo.guardar(prod);
        System.out.println("✔ Producto actualizado correctamente.");
    }

    /** Muestra todos los productos activos con su categoría */
    private static void listarProductos() {
        List<Producto> lista = productoRepo.listarActivos();
        if (lista.isEmpty()) {
            System.out.println("  (No hay productos activos)");
            return;
        }
        System.out.println("\n  ID  | Nombre              | Precio     | Stock | Categoría");
        System.out.println("  ────┼─────────────────────┼────────────┼───────┼────────────────");
        for (Producto p : lista) {
            String cat = (p.getCategoria() != null) ? p.getCategoria().getNombre() : "Sin categoría";
            System.out.printf("  %-4d| %-21s| $%-9.2f| %-5d | %s%n",
                    p.getId(), p.getNombre(), p.getPrecio(), p.getStock(), cat);
        }
    }

    //  SUBMENU REPORTES

    private static void menuReportes() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── REPORTES ────────────────────");
            System.out.println("  1. Productos por categoría");
            System.out.println("  0. Volver");
            System.out.print("Seleccione: ");
            switch (leerInt()) {
                case 1  -> reporteProductosPorCategoria();
                case 0  -> volver = true;
                default -> System.out.println("⚠ Opción inválida.");
            }
        }
    }

    private static void reporteProductosPorCategoria() {
        System.out.println("\n> Productos por Categoría");

        List<Categoria> categorias = categoriaRepo.listarActivos();
        if (categorias.isEmpty()) {
            System.out.println("✘ No hay categorías activas.");
            return;
        }
        System.out.println("Categorías disponibles:");
        listarCategorias();

        System.out.print("ID de categoría: ");
        Long id = leerLong();
        if (id == null) { System.out.println("✘ ID inválido."); return; }

        Optional<Categoria> catOpt = categoriaRepo.buscarPorId(id);
        if (catOpt.isEmpty() || catOpt.get().isEliminado()) {
            System.out.println("✘ Error: categoría no encontrada.");
            return;
        }

        // Llamada al método JPQL del ProductoRepository
        List<Producto> productos = productoRepo.buscarPorCategoria(id);

        if (productos.isEmpty()) {
            System.out.println("No hay productos activos en la categoría \""
                             + catOpt.get().getNombre() + "\".");
            return;
        }

        System.out.println("\nProductos de \"" + catOpt.get().getNombre() + "\":");
        System.out.println("  ID  | Nombre              | Precio     | Stock");
        System.out.println("  ────┼─────────────────────┼────────────┼───────");
        for (Producto p : productos) {
            System.out.printf("  %-4d| %-21s| $%-9.2f| %d%n",
                    p.getId(), p.getNombre(), p.getPrecio(), p.getStock());
        }
    }

    //  HELPERS

    private static int leerInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    private static Long leerLong() {
        try {
            return Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
