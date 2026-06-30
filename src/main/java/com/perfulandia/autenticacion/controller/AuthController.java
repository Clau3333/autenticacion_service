package com.perfulandia.autenticacion.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.perfulandia.autenticacion.dto.LoginRequest;
import com.perfulandia.autenticacion.dto.LoginResponse;
import com.perfulandia.autenticacion.dto.ValidarTokenRequest;
import com.perfulandia.autenticacion.dto.ValidarTokenResponse;
import com.perfulandia.autenticacion.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/validar-token")
    public ResponseEntity<ValidarTokenResponse> validarToken(@Valid @RequestBody ValidarTokenRequest request) {
        return ResponseEntity.ok(authService.validarToken(request.getToken()));
    }

    @GetMapping("/tiene-permiso")
    public ResponseEntity<Map<String, Object>> tienePermiso(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam String permiso
    ) {
        boolean permitido = authService.tienePermiso(authorization, permiso);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("permitido", permitido);
        respuesta.put("permiso", permiso);

        return ResponseEntity.ok(respuesta);
    }
}