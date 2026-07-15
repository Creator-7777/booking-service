package com.alena.booking.service;

import com.alena.booking.entity.VerifiedCustomer;
import com.alena.booking.repository.VerifiedCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifiedCustomerService {

    private final VerifiedCustomerRepository repository;

    public boolean isVerified(String phone) {
        return repository.existsByPhone(phone);
    }

    public void saveVerified(String name, String phone) {

        if (repository.existsByPhone(phone))
            return;

        VerifiedCustomer customer = new VerifiedCustomer();

        customer.setName(name);
        customer.setPhone(phone);
        customer.setVerified(true);

        repository.save(customer);
    }
}