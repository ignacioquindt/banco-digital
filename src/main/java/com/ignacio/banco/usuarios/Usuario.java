package com.ignacio.banco.usuarios;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "credenciales_version", nullable = false)
    private long credencialesVersion = 0;

    public long getCredencialesVersion() { return credencialesVersion; }

    public void actualizarNombre(String nombre) { this.nombre = nombre; }

    public void cambiarPassword(String hash) {
        this.passwordHash = hash;
        this.credencialesVersion++;
    }

    protected Usuario() {
    }

    public Usuario(String nombre, String email, String passwordHash) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActivo() {
        return activo;
    }
}