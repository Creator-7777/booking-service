package com.alena.booking.repository;

import com.alena.booking.entity.VerifiedCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerifiedCustomerRepository
        extends JpaRepository<VerifiedCustomer, Long> {

    Optional<VerifiedCustomer> findByPhone(String phone);

    boolean existsByPhone(String phone);
}