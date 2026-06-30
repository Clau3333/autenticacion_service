package com.perfulandia.autenticacion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.perfulandia.autenticacion.dto.LoginRequest;
import com.perfulandia.autenticacion.dto.LoginResponse;
import com.perfulandia.autenticacion.dto.ValidarTokenResponse;

class AuthServiceTest {

    private AuthService authService;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "perfulandia-clave-secreta-para-firmar-tokens-jwt-2026"
        );

        ReflectionTestUtils.setField(jwtService, "expirationMinutes", 60L);

        authService = new AuthService(jwtService);
    }

    @Test
    void login_deberiaRetornarTokenCuandoCredencialesSonCorrectas() {
        LoginRequest request = new LoginRequest("admin@perfulandia.cl", "admin123");

        LoginResponse response = authService.login(request);

        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertEquals(1L, response.getIdUsuario());
        assertEquals("admin@perfulandia.cl", response.getCorreo());
        assertEquals("ADMINISTRADOR", response.getRol());
        assertTrue(response.getPermisos().contains("CREAR_USUARIO"));
    }

    @Test
    void login_deberiaNormalizarCorreo() {
        LoginRequest request = new LoginRequest("  ADMIN@PERFULANDIA.CL  ", "admin123");

        LoginResponse response = authService.login(request);

        assertEquals("admin@perfulandia.cl", response.getCorreo());
    }

    @Test
    void login_deberiaLanzarErrorCuandoCredencialesSonInvalidas() {
        LoginRequest request = new LoginRequest("admin@perfulandia.cl", "mala123");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));

        assertEquals("Credenciales inválidas", ex.getMessage());
    }

    @Test
    void validarToken_deberiaRetornarInvalidoCuandoTokenEsNulo() {
        ValidarTokenResponse response = authService.validarToken(null);

        assertFalse(response.isValido());
        assertEquals("Token no informado", response.getMensaje());
    }

    @Test
    void validarToken_deberiaRetornarInvalidoCuandoTokenEstaVacio() {
        ValidarTokenResponse response = authService.validarToken("   ");

        assertFalse(response.isValido());
        assertEquals("Token no informado", response.getMensaje());
    }

    @Test
    void validarToken_deberiaRetornarInvalidoCuandoTokenNoSirve() {
        ValidarTokenResponse response = authService.validarToken("token-invalido");

        assertFalse(response.isValido());
        assertEquals("Token inválido o expirado", response.getMensaje());
    }

    @Test
    void validarToken_deberiaRetornarDatosCuandoTokenEsValido() {
        LoginResponse login = authService.login(new LoginRequest("admin@perfulandia.cl", "admin123"));

        ValidarTokenResponse response = authService.validarToken(login.getToken());

        assertTrue(response.isValido());
        assertEquals("Token válido", response.getMensaje());
        assertEquals(1L, response.getIdUsuario());
        assertEquals("admin@perfulandia.cl", response.getCorreo());
        assertEquals("ADMINISTRADOR", response.getRol());
        assertTrue(response.getPermisos().contains("CREAR_USUARIO"));
    }

    @Test
    void tienePermiso_deberiaRetornarTrueCuandoPermisoExiste() {
        LoginResponse login = authService.login(new LoginRequest("admin@perfulandia.cl", "admin123"));

        boolean permitido = authService.tienePermiso("Bearer " + login.getToken(), "CREAR_USUARIO");

        assertTrue(permitido);
    }

    @Test
    void tienePermiso_deberiaRetornarFalseCuandoPermisoNoExiste() {
        LoginResponse login = authService.login(new LoginRequest("admin@perfulandia.cl", "admin123"));

        boolean permitido = authService.tienePermiso("Bearer " + login.getToken(), "ELIMINAR_PRODUCTO");

        assertFalse(permitido);
    }

    @Test
    void tienePermiso_deberiaRetornarFalseCuandoTokenEsInvalido() {
        boolean permitido = authService.tienePermiso("token-invalido", "CREAR_USUARIO");

        assertFalse(permitido);
    }
}