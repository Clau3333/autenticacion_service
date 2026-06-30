package com.perfulandia.autenticacion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.perfulandia.autenticacion.dto.LoginRequest;

class AuthServiceBranchTest {

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
    void login_deberiaLanzarErrorCuandoCorreoEsInvalido() {
        LoginRequest request = new LoginRequest("otro@perfulandia.cl", "admin123");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));

        assertEquals("Credenciales inválidas", ex.getMessage());
    }

    @Test
    void tienePermiso_deberiaRetornarFalseCuandoTokenValidoNoTienePermisos() {
        String token = jwtService.generarToken(
                1L,
                "admin@perfulandia.cl",
                "ADMINISTRADOR",
                null
        );

        boolean permitido = authService.tienePermiso(token, "CREAR_USUARIO");

        assertFalse(permitido);
    }
}