package com.ignacio.banco.recuperacion;



import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface RecuperacionRepository extends JpaRepository<RecuperacionToken,String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RecuperacionToken t WHERE t.hash = :hash")
    Optional<RecuperacionToken> buscarBloqueado(@Param("hash") String hash);
}
