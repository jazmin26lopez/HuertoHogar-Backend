package com.example.huertoHogar_Backend.seguridad;

import com.example.huertoHogar_Backend.usuario.UsuarioModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/**
 * Crea y valida los tokens JWT de la API.
 *
 * El token lleva el id del usuario (subject), su correo y su rol, y esta firmado
 * con HMAC-SHA256 usando el secreto app.jwt.secret.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long minutosDeVida;

    public JwtService(
            @Value("${app.jwt.secret:}") String secreto,
            @Value("${app.jwt.expiracion-minutos:60}") long minutosDeVida) {

        if (secreto == null
                || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "Falta el secreto JWT: define JWT_SECRET o app.jwt.secret "
                            + "(minimo 32 caracteres) en application-local.properties");
        }

        this.clave = Keys.hmacShaKeyFor(
                secreto.getBytes(StandardCharsets.UTF_8));
        this.minutosDeVida = minutosDeVida;
    }

    public long getMinutosDeVida() {
        return minutosDeVida;
    }

    /** Genera un token firmado para el usuario. */
    public String generar(UsuarioModel usuario) {
        Instant ahora = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(usuario.getUsuarioId()))
                .claim("correo", usuario.getCorreo())
                .claim("rol", usuario.getRol())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(
                        ahora.plus(minutosDeVida, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    /**
     * Lee un token. Devuelve vacio si esta mal formado, fue alterado,
     * fue firmado con otro secreto o ya expiro.
     */
    public Optional<Claims> leer(String token) {
        try {
            return Optional.of(Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
