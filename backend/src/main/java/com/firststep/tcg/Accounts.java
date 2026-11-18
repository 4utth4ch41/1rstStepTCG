package com.firststep.tcg;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface Accounts extends JpaRepository<Account,Long> {
 Optional<Account> findByEmail(String email);
}
