package com.example.huertoHogar_Backend.seguridad;

import com.example.huertoHogar_Backend.auth.AuthController;
import com.example.huertoHogar_Backend.auth.AuthService;
import com.example.huertoHogar_Backend.boleta.BoletaController;
import com.example.huertoHogar_Backend.boleta.BoletaService;
import com.example.huertoHogar_Backend.config.SecurityConfig;
import com.example.huertoHogar_Backend.producto.ProductoController;
import com.example.huertoHogar_Backend.producto.ProductoService;
import com.example.huertoHogar_Backend.usuario.UsuarioController;
import com.example.huertoHogar_Backend.usuario.UsuarioModel;
import com.example.huertoHogar_Backend.usuario.UsuarioRepository;
import com.example.huertoHogar_Backend.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprueba las reglas de acceso por rol, con tokens reales y sin base de datos.
 */
@WebMvcTest(controllers = {
        UsuarioController.class, ProductoController.class,
        BoletaController.class, AuthController.class})
@Import({SecurityConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "app.jwt.secret=secreto-de-prueba-con-mas-de-32-caracteres-1234",
        "app.jwt.expiracion-minutos=60"})
class SeguridadRolesTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;

    @MockitoBean UsuarioRepository usuarioRepository;
    @MockitoBean UsuarioService usuarioService;
    @MockitoBean ProductoService productoService;
    @MockitoBean BoletaService boletaService;
    @MockitoBean AuthService authService;

    /** Crea un usuario "en la BD" y devuelve su cabecera Authorization. */
    private String tokenDe(long id, String rol) {
        UsuarioModel u = new UsuarioModel();
        u.setUsuarioId(id);
        u.setCorreo(rol.toLowerCase() + "@duoc.cl");
        u.setRol(rol);
        u.setActivo("S");
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(u));
        return "Bearer " + jwt.generar(u);
    }

    @Test
    void productosSeVenSinIniciarSesion() throws Exception {
        when(productoService.listarProductos()).thenReturn(List.of());

        mvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
    }

    @Test
    void usuariosSinTokenDa401() throws Exception {
        mvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuariosConTokenBasuraDa401() throws Exception {
        mvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer basura"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeVerUsuarios() throws Exception {
        mvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", tokenDe(1, "Cliente")))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorNoPuedeVerUsuarios() throws Exception {
        mvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", tokenDe(2, "Vendedor")))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorPuedeVerUsuarios() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of());

        mvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", tokenDe(3, "Administrador")))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNoPuedeCrearProductos() throws Exception {
        mvc.perform(post("/api/v1/productos")
                        .header("Authorization", tokenDe(1, "Cliente"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenNoSePuedeEliminarProductos() throws Exception {
        mvc.perform(delete("/api/v1/productos/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void vendedorNoPuedeEliminarNiCrearProductos() throws Exception {
        String token = tokenDe(2, "Vendedor");

        mvc.perform(delete("/api/v1/productos/1")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/productos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorNoPuedeComprar() throws Exception {
        mvc.perform(post("/api/v1/boletas")
                        .header("Authorization", tokenDe(2, "Vendedor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void clienteYAdministradorSiPuedenComprar() throws Exception {
        mvc.perform(post("/api/v1/boletas")
                        .header("Authorization", tokenDe(1, "Cliente"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/boletas")
                        .header("Authorization", tokenDe(3, "Administrador"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void administradorSiPuedeEliminarProductos() throws Exception {
        mvc.perform(delete("/api/v1/productos/1")
                        .header("Authorization", tokenDe(3, "Administrador")))
                .andExpect(status().isNoContent());
    }

    @Test
    void vendedorPuedeVerLasBoletasPeroElClienteNo() throws Exception {
        when(boletaService.listarBoletas()).thenReturn(List.of());

        mvc.perform(get("/api/v1/boletas")
                        .header("Authorization", tokenDe(2, "Vendedor")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/boletas")
                        .header("Authorization", tokenDe(1, "Cliente")))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioDesactivadoPierdeElAccesoAunConTokenVigente() throws Exception {
        String token = tokenDe(4, "Administrador");
        UsuarioModel desactivado = new UsuarioModel();
        desactivado.setUsuarioId(4L);
        desactivado.setRol("Administrador");
        desactivado.setActivo("N");
        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(desactivado));

        mvc.perform(get("/api/v1/usuarios").header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }
}
