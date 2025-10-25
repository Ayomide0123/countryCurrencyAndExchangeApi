package com.hng.countryCurrencyAndExchangeApi.controller;

import com.hng.countryCurrencyAndExchangeApi.model.CountryEntity;
import com.hng.countryCurrencyAndExchangeApi.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @PostMapping("/countries/refresh")
    public ResponseEntity<?> refreshCountries() {
        try {
            String message = countryService.refreshCountries();
            return ResponseEntity.ok(Map.of("message", message));
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("External data source unavailable")) {
                return ResponseEntity.status(503).body(Map.of(
                        "error", "External data source unavailable",
                        "details", ex.getMessage()
                ));
            }
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }


    @GetMapping("/countries")
    public ResponseEntity<?> getAllCountries(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String sort) {

        List<CountryEntity> countries = countryService.getAllCountries(region, currency, sort);
        if (countries.isEmpty()){
            return ResponseEntity.status(404).body(Map.of("error", "No Countries found"));
        }
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/countries/{name}")
    public ResponseEntity<?> getCountryByName(@PathVariable String name) {
        try {
            CountryEntity country = countryService.getCountryByName(name);
            return ResponseEntity.ok(country);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error", "Country not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @DeleteMapping("/countries/{name}")
    public ResponseEntity<?> deleteCountryByName(@PathVariable String name) {
        try {
            countryService.deleteCountryByName(name);
            return ResponseEntity.ok(Map.of("message", "Country deleted successfully"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error","Country not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        try {
            Map<String, Object> status = countryService.getStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/countries/image")
    public ResponseEntity<?> getSummaryImage() {
        File imageFile = new File("cache/summary.png");

        if (!imageFile.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Summary image not found"));
        }

        try {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not read image"));
        }
    }
}
