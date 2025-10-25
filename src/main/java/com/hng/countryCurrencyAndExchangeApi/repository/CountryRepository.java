package com.hng.countryCurrencyAndExchangeApi.repository;

import com.hng.countryCurrencyAndExchangeApi.model.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<CountryEntity, Long> {
    Optional<CountryEntity> findByNameIgnoreCase(String name);
}
