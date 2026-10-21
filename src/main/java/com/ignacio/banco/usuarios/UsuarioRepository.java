package com.ignacio.banco.usuarios;



import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT u FROM Usuario u WHERE u.id = :id")
    Optional<Usuario> buscarBloqueado(@org.springframework.data.repository.query.Param("id") Long id);

    boolean existsByEmail(String email);
}