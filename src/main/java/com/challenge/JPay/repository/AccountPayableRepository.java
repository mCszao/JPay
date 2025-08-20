package com.challenge.JPay.repository;

import com.challenge.JPay.model.AccountPayable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountPayableRepository extends JpaRepository<AccountPayable, Long> {
}
