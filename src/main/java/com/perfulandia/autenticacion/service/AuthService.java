package com.perfulandia.autenticacion.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.perfulandia.autenticacion.dto.LoginRequest;
import com.perfulandia.autenticacion.dto.LoginResponse;
import com.perfulandia.autenticacion.dto.ValidarTokenResponse;

import io.jsonwebtoken.Claims;

@Service
public class AuthService {

    private final JwtService jwtService;

    public AuthService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        String correo = request.getCorreo().trim().toLowerCase();
        String password = request.getPassword();

        if (!"admin@perfulandia.cl".equals(correo) || !"admin123".equals(password)) {
            throw new RuntimeException("Credenciales inválidas");
        }

        Long idUsuario = 1L;
        String rol = "ADMINISTRADOR";
        List<String> permisos = List.of(
                "CREAR_USUARIO",
                "EDITAR_USUARIO",
                "ELIMINAR_USUARIO",
                "VER_USUARIOS",
                "GESTIONAR_ROLES",
                "GESTIONAR_PERMISOS"
        );

        String token = jwtService.generarToken(idUsuario, correo, rol, permisos);

        return new LoginResponse(
                token,
                "Bearer",
                idUsuario,
                correo,
                rol,
                permisos
        );
    }

    public ValidarTokenResponse validarToken(String token) {
        if (token == null || token.isBlank()) {
            return ValidarTokenResponse.invalido("Token no informado");
        }

        if (!jwtService.tokenEsValido(token)) {
            return ValidarTokenResponse.invalido("Token inválido o expirado");
        }

        Claims claims = jwtService.obtenerClaims(token);

        Long idUsuario = claims.get("idUsuario", Long.class);
        String correo = claims.getSubject();
        String rol = claims.get("rol", String.class);

        @SuppressWarnings("unchecked")
        List<String> permisos = claims.get("permisos", List.class);

        return new ValidarTokenResponse(
                true,
                "Token válido",
                idUsuario,
                correo,
                rol,
                permisos
        );
    }

    public boolean tienePermiso(String token, String permiso) {
        ValidarTokenResponse respuesta = validarToken(token);

        return respuesta.isValido()
                && respuesta.getPermisos() != null
                && respuesta.getPermisos().contains(permiso);
    }
}