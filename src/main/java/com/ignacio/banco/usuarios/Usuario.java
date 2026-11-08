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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol = Rol.ROLE_CLIENTE;

    @Column(name = "credenciales_version", nullable = false)
    private long credencialesVersion = 0;

    public long getCredencialesVersion() { return credencialesVersion; }

    public void actualizarNombre(String nombre) { this.nombre = nombre; }

    public Rol getRol() { return rol; }

    public void asignarRol(Rol rol) {
        if (rol != null) {
            this.rol = rol;
        }
    }

    public void cambiarPassword(String hash) {
        this.passwordHash = hash;
        this.credencialesVersion++;
    }

    protected Usuario() {
    }

    public Usuario(String nombre, String email, String passwordHash) {
        this(nombre, email, passwordHash, Rol.ROLE_CLIENTE);
    }

    public Usuario(String nombre, String email, String passwordHash, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol != null ? rol : Rol.ROLE_CLIENTE;
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

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}