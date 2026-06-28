package com.tp.jpa;

import com.tp.jpa.model.Categoria;
import com.tp.jpa.model.DetallePedido;
import com.tp.jpa.model.Pedido;
import com.tp.jpa.model.Producto;
import com.tp.jpa.model.Usuario;
import com.tp.jpa.model.enums.Estado;
import com.tp.jpa.model.enums.FormaPago;
import com.tp.jpa.model.enums.Rol;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.PedidoRepository;
import com.tp.jpa.repository.ProductoRepository;
import com.tp.jpa.repository.UsuarioRepository;
import com.tp.jpa.util.JPAUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Locale;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final CategoriaRepository categoriaRepo = new CategoriaRepository();
    private static final ProductoRepository  productoRepo  = new ProductoRepository();
    private static final UsuarioRepository   usuarioRepo   = new UsuarioRepository();
    private static final PedidoRepository    pedidoRepo    = new PedidoRepository();
    private static       Usuario             usuarioActual;

    public static void main(String[] args) {
        login();
        boolean salir = false;
        while (!salir) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║    SISTEMA DE GESTIÓN JPA    ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║  1. Categorías               ║");
            System.out.println("║  2. Productos                ║");
            System.out.println("║  3. Reportes                 ║");
            System.out.println("║  4. Usuarios                 ║");
            System.out.println("║  5. Pedidos                  ║");
            System.out.println("║  0. Salir                    ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.print("Seleccione: ");
            switch (leerInt()) {
                case 1  -> menuCategorias();
                case 2  -> menuProductos();
                case 3  -> menuReportes();
                case 4  -> menuUsuarios();
                case 5  -> menuPedidos();
                case 0  -> salir = true;
                default -> System.out.println(" Opción inválida.");
            }
        }
        JPAUtil.close();
        System.out.println("¡Adios!");
    }

    //  LOGIN / REGISTRATION

    private static void login() {
        while (true) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║        INICIO SESIÓN         ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Contraseña: ");
            String password = scanner.nextLine().trim();

            Optional<Usuario> opt = usuarioRepo.buscarPorMail(email);
            if (opt.isPresent()) {
                Usuario user = opt.get();
                if (user.getContrasena().equals(password)) {
                    usuarioActual = user;
                    System.out.println("✔ Bienvenido, " + user.getMail() + " (" + user.getRol() + ")");
                    return;
                } else {
                    System.out.println("✘ Contraseña incorrecta.");
                }
            } else {
                System.out.println("✘ Usuario no encontrado.");
                System.out.print("¿Registrarse? (s/n): ");
                String respuesta = scanner.nextLine().trim().toLowerCase();
                if (respuesta.equals("s")) {
                    registrar();
                }
            }
        }
    }

    private static void registrar() {
        System.out.println("\n> Registro de Usuario");

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            System.out.println("✘ Error: el email no puede estar vacío.");
            return;
        }

        Optional<Usuario> existente = usuarioRepo.buscarPorMail(email);
        if (existente.isPresent()) {
            System.out.println("✘ El mail ya está registrado.");
            return;
        }

        System.out.print("Contraseña: ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) {
            System.out.println("✘ Error: la contraseña no puede estar vacía.");
            return;
        }

        Usuario user = new Usuario();
        user.setMail(email);
        user.setContrasena(password);

        // First user = ADMIN, subsequent = USER
        List<Usuario> todos = usuarioRepo.listarActivos();
        user.setRol(todos.isEmpty() ? Rol.ADMIN : Rol.USUARIO);

        usuarioRepo.guardar(user);
        System.out.println("✔ Usuario registrado correctamente.");
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

        Producto producto = new Producto(nombre, precio, descripcion, stock, imagen, true);
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
        Map<Long, String> prodCats = buildProductoCategoriaMap();
        System.out.println("\n  ID  | Nombre              | Precio     | Stock | Categoría");
        System.out.println("  ────┼─────────────────────┼────────────┼───────┼────────────────");
        for (Producto p : lista) {
            String cat = prodCats.getOrDefault(p.getId(), "—");
            System.out.printf("  %-4d| %-21s| $%-9.2f| %-5d | %s%n",
                    p.getId(), p.getNombre(), p.getPrecio(), p.getStock(), cat);
        }
    }

    //  SUBMENU USUARIOS

    private static void menuUsuarios() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── USUARIOS ────────────────────");
            System.out.println("  1. Alta");
            System.out.println("  2. Modificar");
            System.out.println("  3. Baja lógica");
            System.out.println("  4. Listado");
            System.out.println("  5. Buscar por mail");
            System.out.println("  0. Volver");
            System.out.print("Seleccione: ");
            switch (leerInt()) {
                case 1  -> altaUsuario();
                case 2  -> modificarUsuario();
                case 3  -> bajaUsuario();
                case 4  -> listarUsuarios();
                case 5  -> buscarUsuarioPorMail();
                case 0  -> volver = true;
                default -> System.out.println("⚠ Opción inválida.");
            }
        }
    }

    private static void listarUsuarios() {
        List<Usuario> lista = usuarioRepo.listarActivos();
        if (lista.isEmpty()) {
            System.out.println("  (No hay usuarios activos)");
            return;
        }
        System.out.println("\n  ID  | Mail                     | Rol");
        System.out.println("  ────┼──────────────────────────┼──────────");
        for (Usuario u : lista) {
            System.out.printf("  %-4d| %-25s| %s%n",
                    u.getId(), u.getMail(), u.getRol());
        }
    }

    private static void altaUsuario() {
        System.out.println("\n> Alta de Usuario");

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine().trim();
        if (nombre.isEmpty()) { System.out.println("✘ Error: el nombre no puede estar vacío."); return; }

        System.out.print("Apellido: ");
        String apellido = scanner.nextLine().trim();
        if (apellido.isEmpty()) { System.out.println("✘ Error: el apellido no puede estar vacío."); return; }

        System.out.print("Mail: ");
        String mail = scanner.nextLine().trim();
        if (mail.isEmpty()) { System.out.println("✘ Error: el mail no puede estar vacío."); return; }

        // Check unique email
        if (usuarioRepo.buscarPorMail(mail).isPresent()) {
            System.out.println("✘ Error: ya existe un usuario con ese mail.");
            return;
        }

        System.out.print("Celular (opcional): ");
        String celular = scanner.nextLine().trim();

        System.out.print("Contraseña: ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) { System.out.println("✘ Error: la contraseña no puede estar vacía."); return; }

        System.out.println("Roles:");
        System.out.println("  1. ADMIN");
        System.out.println("  2. USUARIO");
        System.out.print("Seleccione: ");
        Rol rol = (leerInt() == 1) ? Rol.ADMIN : Rol.USUARIO;

        Usuario user = new Usuario();
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setMail(mail);
        user.setCelular(celular.isEmpty() ? null : celular);
        user.setContrasena(password);
        user.setRol(rol);

        Usuario guardado = usuarioRepo.guardar(user);
        System.out.println("✔ Usuario creado. ID: " + guardado.getId());
    }

    private static void modificarUsuario() {
        System.out.println("\n> Modificar Usuario");
        listarUsuarios();

        System.out.print("ID del usuario a modificar: ");
        Long id = leerLong();
        if (id == null) { System.out.println("✘ ID inválido."); return; }

        Optional<Usuario> opt = usuarioRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("✘ Error: no existe un usuario activo con ese ID.");
            return;
        }

        Usuario user = opt.get();
        System.out.println("Valores actuales:");
        System.out.println("  Nombre: " + user.getNombre());
        System.out.println("  Apellido: " + user.getApellido());
        System.out.println("  Mail: " + user.getMail());
        System.out.println("  Celular: " + (user.getCelular() != null ? user.getCelular() : "—"));
        System.out.println("(Presioná Enter para conservar el valor actual)");

        System.out.print("Nuevo nombre [" + user.getNombre() + "]: ");
        String nuevoNombre = scanner.nextLine().trim();
        if (!nuevoNombre.isEmpty()) user.setNombre(nuevoNombre);

        System.out.print("Nuevo apellido [" + user.getApellido() + "]: ");
        String nuevoApellido = scanner.nextLine().trim();
        if (!nuevoApellido.isEmpty()) user.setApellido(nuevoApellido);

        System.out.print("Nuevo mail [" + user.getMail() + "]: ");
        String nuevoMail = scanner.nextLine().trim();
        if (!nuevoMail.isEmpty()) {
            // Validate new mail is not taken by another user
            Optional<Usuario> existente = usuarioRepo.buscarPorMail(nuevoMail);
            if (existente.isPresent() && !existente.get().getId().equals(user.getId())) {
                System.out.println("✘ Error: el mail ya está en uso por otro usuario.");
                return;
            }
            user.setMail(nuevoMail);
        }

        System.out.print("Nuevo celular [" + (user.getCelular() != null ? user.getCelular() : "—") + "]: ");
        String nuevoCelular = scanner.nextLine().trim();
        if (!nuevoCelular.isEmpty()) user.setCelular(nuevoCelular);

        System.out.print("Nueva contraseña (Enter para mantener): ");
        String nuevaPass = scanner.nextLine().trim();
        if (!nuevaPass.isEmpty()) user.setContrasena(nuevaPass);

        usuarioRepo.guardar(user);
        System.out.println("✔ Usuario actualizado correctamente.");
    }

    private static void bajaUsuario() {
        System.out.println("\n> Baja de Usuario");
        listarUsuarios();

        System.out.print("ID del usuario a dar de baja: ");
        Long id = leerLong();
        if (id == null) { System.out.println("✘ ID inválido."); return; }

        Optional<Usuario> opt = usuarioRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("✘ Error: no existe un usuario activo con ese ID.");
            return;
        }

        Usuario user = opt.get();
        usuarioRepo.eliminarLogico(id);
        System.out.println("✔ Usuario \"" + user.getNombre() + " " + user.getApellido() + "\" dado de baja correctamente. Sus pedidos permanecen en el sistema.");
    }

    private static void buscarUsuarioPorMail() {
        System.out.println("\n> Buscar Usuario por Mail");
        System.out.print("Mail: ");
        String mail = scanner.nextLine().trim();
        if (mail.isEmpty()) { System.out.println("✘ Error: el mail no puede estar vacío."); return; }

        Optional<Usuario> opt = usuarioRepo.buscarPorMail(mail);
        if (opt.isEmpty()) {
            System.out.println("✘ No existe un usuario activo con ese mail.");
            return;
        }

        Usuario user = opt.get();
        System.out.println("  ID: " + user.getId());
        System.out.println("  Nombre: " + user.getNombre());
        System.out.println("  Apellido: " + user.getApellido());
        System.out.println("  Mail: " + user.getMail());
        System.out.println("  Celular: " + (user.getCelular() != null ? user.getCelular() : "—"));
        System.out.println("  Rol: " + user.getRol());
    }

    //  SUBMENU PEDIDOS

    private static void menuPedidos() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── PEDIDOS ─────────────────────");
            System.out.println("  1. Crear pedido");
            System.out.println("  2. Cambiar estado (ADMIN)");
            System.out.println("  3. Listar pedidos");
            System.out.println("  4. Baja lógica");
            System.out.println("  0. Volver");
            System.out.print("Seleccione: ");
            switch (leerInt()) {
                case 1  -> crearPedido();
                case 2  -> cambiarEstadoPedido();
                case 3  -> listarPedidos();
                case 4  -> bajaPedido();
                case 0  -> volver = true;
                default -> System.out.println("⚠ Opción inválida.");
            }
        }
    }

    private static void crearPedido() {
        System.out.println("\n> Crear Pedido");

        Map<Long, String> prodCats = buildProductoCategoriaMap();
        List<Producto> productos = productoRepo.listarActivos();
        if (productos.isEmpty()) {
            System.out.println("✘ No hay productos disponibles.");
            return;
        }

        System.out.println("\nProductos disponibles:");
        System.out.println("  ID  | Nombre              | Precio     | Categoría");
        System.out.println("  ────┼─────────────────────┼────────────┼────────────────");
        for (Producto p : productos) {
            String cat = prodCats.getOrDefault(p.getId(), "—");
            System.out.printf("  %-4d| %-21s| $%-9.2f| %s%n",
                    p.getId(), p.getNombre(), p.getPrecio(), cat);
        }

        Pedido pedido = new Pedido(LocalDate.now(), Estado.PENDIENTE, FormaPago.EFECTIVO);

        boolean agregar = true;
        while (agregar) {
            System.out.print("\nID del producto (0 para cancelar): ");
            Long prodId = leerLong();
            if (prodId == null || prodId == 0) {
                if (pedido.getDetalles().isEmpty()) {
                    System.out.println("✘ Pedido cancelado.");
                    return;
                }
                break;
            }

            Optional<Producto> opt = productoRepo.buscarPorId(prodId);
            if (opt.isEmpty() || opt.get().isEliminado()) {
                System.out.println("✘ Producto no encontrado.");
                continue;
            }

            Producto prod = opt.get();
            System.out.print("Cantidad: ");
            int cantidad = leerInt();
            if (cantidad <= 0) {
                System.out.println("✘ Cantidad inválida.");
                continue;
            }

            pedido.addDetallePedido(cantidad, prod);
            System.out.println("✔ Agregado: " + prod.getNombre() + " x" + cantidad + " = $" + (cantidad * prod.getPrecio()));

            System.out.print("¿Agregar otro producto? (s/n): ");
            agregar = scanner.nextLine().trim().toLowerCase().equals("s");
        }

        // Show summary
        System.out.println("\n── Resumen del pedido ─────────");
        double total = 0;
        for (DetallePedido d : pedido.getDetalles()) {
            System.out.printf("  %-21s x%d  $%.2f%n",
                    d.getProducto().getNombre(), d.getCantidad(), d.getSubtotal());
            total += d.getSubtotal();
        }
        System.out.printf("  Total: $%.2f%n", total);

        System.out.print("¿Confirmar pedido? (s/n): ");
        if (!scanner.nextLine().trim().toLowerCase().equals("s")) {
            System.out.println("✘ Pedido cancelado.");
            return;
        }

        // Select payment method
        System.out.println("\nFormas de pago:");
        System.out.println("  1. EFECTIVO");
        System.out.println("  2. TARJETA");
        System.out.println("  3. TRANSFERENCIA");
        System.out.print("Seleccione: ");
        FormaPago formaPago;
        switch (leerInt()) {
            case 1 -> formaPago = FormaPago.EFECTIVO;
            case 2 -> formaPago = FormaPago.TARJETA;
            case 3 -> formaPago = FormaPago.TRANSFERENCIA;
            default -> {
                System.out.println("✘ Opción inválida, se usará EFECTIVO.");
                formaPago = FormaPago.EFECTIVO;
            }
        }

        pedido.setFormaPago(formaPago);
        pedido.calcularTotal();

        Pedido guardado = pedidoRepo.guardar(pedido);

        // Reduce stock for each product
        for (DetallePedido d : pedido.getDetalles()) {
            Producto prod = d.getProducto();
            prod.setStock(prod.getStock() - d.getCantidad());
            productoRepo.guardar(prod);
        }

        // Associate with current user (unidirectional: Usuario -> pedidos)
        usuarioActual.getPedidos().add(guardado);
        usuarioRepo.guardar(usuarioActual);

        System.out.println("✔ Pedido #" + guardado.getId() + " creado correctamente.");
    }

    private static void cambiarEstadoPedido() {
        if (usuarioActual.getRol() != Rol.ADMIN) {
            System.out.println("✘ Solo administradores pueden cambiar estados.");
            return;
        }

        System.out.println("\n> Cambiar Estado de Pedido");
        listarPedidos();

        System.out.print("ID del pedido: ");
        Long id = leerLong();
        if (id == null) { System.out.println("✘ ID inválido."); return; }

        Optional<Pedido> opt = pedidoRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("✘ Pedido no encontrado.");
            return;
        }

        Pedido pedido = opt.get();
        System.out.println("Estado actual: " + pedido.getEstado());

        List<Estado> transiciones = new ArrayList<>();
        switch (pedido.getEstado()) {
            case PENDIENTE -> {
                transiciones.add(Estado.CONFIRMADO);
                transiciones.add(Estado.CANCELADO);
            }
            case CONFIRMADO -> {
                transiciones.add(Estado.TERMINADO);
                transiciones.add(Estado.CANCELADO);
            }
            case TERMINADO, CANCELADO -> {
                System.out.println("✘ El pedido está en estado terminal. No se pueden realizar cambios.");
                return;
            }
        }

        System.out.println("Transiciones disponibles:");
        for (int i = 0; i < transiciones.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + transiciones.get(i));
        }
        System.out.print("Seleccione: ");
        int opcion = leerInt();
        if (opcion < 1 || opcion > transiciones.size()) {
            System.out.println("✘ Opción inválida.");
            return;
        }

        pedido.setEstado(transiciones.get(opcion - 1));
        pedidoRepo.guardar(pedido);
        System.out.println("✔ Estado actualizado a " + pedido.getEstado());
    }

    private static void listarPedidos() {
        List<Pedido> lista = pedidoRepo.listarActivos();
        if (lista.isEmpty()) {
            System.out.println("  (No hay pedidos)");
            return;
        }
        System.out.println("\n  ID  | Fecha      | Estado      | Total      | FormaPago");
        System.out.println("  ────┼────────────┼─────────────┼────────────┼───────────────");
        for (Pedido p : lista) {
            System.out.printf("  %-4d| %-10s | %-11s | $%-8.2f| %s%n",
                    p.getId(), p.getFecha(), p.getEstado(), p.getTotal(), p.getFormaPago());
        }
    }

    private static void bajaPedido() {
        System.out.println("\n> Baja de Pedido");
        listarPedidos();

        System.out.print("ID del pedido: ");
        Long id = leerLong();
        if (id == null) { System.out.println("✘ ID inválido."); return; }

        Optional<Pedido> opt = pedidoRepo.buscarPorId(id);
        if (opt.isEmpty() || opt.get().isEliminado()) {
            System.out.println("✘ Error: no existe un pedido activo con ese ID.");
            return;
        }

        Pedido pedido = opt.get();
        pedidoRepo.eliminarLogico(id);
        System.out.printf("✔ Pedido #%d (total: $%.2f) dado de baja correctamente.%n",
                pedido.getId(), pedido.getTotal());
    }

    //  SUBMENU REPORTES

    private static void menuReportes() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n── REPORTES ────────────────────");
            System.out.println("  1. Productos por categoría");
            System.out.println("  2. Pedidos por usuario");
            System.out.println("  3. Pedidos por estado");
            System.out.println("  4. Total facturado");
            System.out.println("  0. Volver");
            System.out.print("Seleccione: ");
            switch (leerInt()) {
                case 1  -> reporteProductosPorCategoria();
                case 2  -> reportePedidosPorUsuario();
                case 3  -> reportePedidosPorEstado();
                case 4  -> reporteTotalFacturado();
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

        List<Producto> productos = categoriaRepo.buscarProductosPorCategoria(id);

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

    private static void reportePedidosPorUsuario() {
        System.out.println("\n> Pedidos por Usuario");

        List<Usuario> usuarios = usuarioRepo.listarActivos();
        if (usuarios.isEmpty()) {
            System.out.println("  (No hay usuarios activos)");
            return;
        }
        System.out.println("Usuarios disponibles:");
        listarUsuarios();

        System.out.print("ID del usuario: ");
        Long id = leerLong();
        if (id == null) { System.out.println("✘ ID inválido."); return; }

        List<Pedido> pedidos = usuarioRepo.buscarPedidosPorUsuario(id);
        if (pedidos.isEmpty()) {
            System.out.println("  El usuario no tiene pedidos.");
            return;
        }

        System.out.println("\nPedidos del usuario:");
        System.out.println("  ID  | Fecha      | Estado      | Total");
        System.out.println("  ────┼────────────┼─────────────┼────────────");
        for (Pedido p : pedidos) {
            System.out.printf("  %-4d| %-10s | %-11s | $%.2f%n",
                    p.getId(), p.getFecha(), p.getEstado(), p.getTotal());
        }
    }

    private static void reportePedidosPorEstado() {
        System.out.println("\n> Pedidos por Estado");
        System.out.println("Estados disponibles:");
        System.out.println("  1. PENDIENTE");
        System.out.println("  2. CONFIRMADO");
        System.out.println("  3. TERMINADO");
        System.out.println("  4. CANCELADO");
        System.out.print("Seleccione: ");

        Estado estado;
        switch (leerInt()) {
            case 1 -> estado = Estado.PENDIENTE;
            case 2 -> estado = Estado.CONFIRMADO;
            case 3 -> estado = Estado.TERMINADO;
            case 4 -> estado = Estado.CANCELADO;
            default -> {
                System.out.println("✘ Opción inválida.");
                return;
            }
        }

        List<Pedido> pedidos = pedidoRepo.buscarPorEstado(estado);
        if (pedidos.isEmpty()) {
            System.out.println("  No hay pedidos con estado " + estado + ".");
            return;
        }

        System.out.println("\nPedidos con estado " + estado + ":");
        System.out.println("  ID  | Fecha      | Total      | FormaPago");
        System.out.println("  ────┼────────────┼────────────┼───────────────");
        for (Pedido p : pedidos) {
            System.out.printf("  %-4d| %-10s | $%-8.2f| %s%n",
                    p.getId(), p.getFecha(), p.getTotal(), p.getFormaPago());
        }
    }

    private static void reporteTotalFacturado() {
        System.out.println("\n> Total Facturado");
        List<Pedido> terminados = pedidoRepo.buscarPorEstado(Estado.TERMINADO);
        double total = terminados.stream()
                .mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0.0)
                .sum();
        System.out.printf(Locale.US, "Total facturado: $%.2f%n", total);
    }

    //  HELPERS

    /**
     * Builds a Map from product ID to category name by iterating active categories
     * and their products.
     */
    private static Map<Long, String> buildProductoCategoriaMap() {
        Map<Long, String> map = new HashMap<>();
        List<Categoria> categorias = categoriaRepo.listarActivos();
        for (Categoria cat : categorias) {
            List<Producto> productos = categoriaRepo.buscarProductosPorCategoria(cat.getId());
            for (Producto p : productos) {
                map.put(p.getId(), cat.getNombre());
            }
        }
        return map;
    }

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
