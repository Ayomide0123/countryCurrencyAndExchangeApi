package com.hng.countryCurrencyAndExchangeApi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "countries")
public class CountryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String capital;
    private String region;

    @Column(nullable = false)
    private Long population;

    @Column(name = "currency_code")
    private String currencyCode;

    private Double exchangeRate;
    private Double estimatedGdp;
    private String flagUrl;

    private LocalDateTime lastRefreshedAt;
}
