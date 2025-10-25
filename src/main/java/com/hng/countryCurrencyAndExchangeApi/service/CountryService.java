package com.hng.countryCurrencyAndExchangeApi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hng.countryCurrencyAndExchangeApi.model.CountryEntity;
import com.hng.countryCurrencyAndExchangeApi.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String COUNTRIES_API = "https://restcountries.com/v2/all?fields=name,capital,region,population,flag,currencies";
    private static final String EXCHANGE_API = "https://open.er-api.com/v6/latest/USD";

    public String refreshCountries() {
        try {

//            Fetching Countries
            String fetchCountries = restTemplate.getForObject(COUNTRIES_API, String.class);
            if (fetchCountries == null) throw new IOException("Empty Countries response");

            JsonNode countries = objectMapper.readTree(fetchCountries);

//            Fetching Exchange Rates
            String fetchExchangeRates = restTemplate.getForObject(EXCHANGE_API, String.class);
            if (fetchExchangeRates == null) throw new IOException("Empty exchange rate response");

            JsonNode exchangeRates = objectMapper.readTree(fetchExchangeRates).path("rates");

//            Process and Store each Country
            for (JsonNode node : countries) {
                String name = node.path("name").asText(null);
                Long population = node.path("population").asLong(0);
                String capital = node.path("capital").asText(null);
                String region = node.path("region").asText(null);
                String flag = node.path("flag").asText(null);

                JsonNode currencies = node.path("currencies");
                String currencyCode = null;
                Double exchangeRate = null;
                Double estimatedGdp = 0.0;

                if (currencies.isArray() && currencies.size() > 0) {
                    currencyCode = currencies.get(0).path("code").asText();

                    if (currencyCode != null && exchangeRates.has(currencyCode)) {
                        exchangeRate = exchangeRates.get(currencyCode).asDouble();
                        double randomMultiplier = ThreadLocalRandom.current().nextDouble(1000, 2000);
                        estimatedGdp = (population * randomMultiplier)/exchangeRate;
                    } else {
                        exchangeRate = null;
                        estimatedGdp = null;
                    }
                }

//                Validate Required Fields
                if (name == null || population == 0  || currencyCode == null) {
//                    Skip invalid entries
                    continue;
                }

                // Check if the country already exists
                Optional<CountryEntity> optionalCountry = countryRepository.findByNameIgnoreCase(name);
                CountryEntity country;

                if (optionalCountry.isPresent()) {
                    country = optionalCountry.get();
                    country.setCapital(capital);
                    country.setRegion(region);
                    country.setPopulation(population);
                    country.setCurrencyCode(currencyCode);
                    country.setExchangeRate(exchangeRate);
                    country.setEstimatedGdp(estimatedGdp);
                    country.setFlagUrl(flag);
                    country.setLastRefreshedAt(LocalDateTime.now());
                } else {
                    country = CountryEntity.builder()
                            .name(name)
                            .capital(capital)
                            .region(region)
                            .population(population)
                            .currencyCode(currencyCode)
                            .exchangeRate(exchangeRate)
                            .estimatedGdp(estimatedGdp)
                            .flagUrl(flag)
                            .lastRefreshedAt(LocalDateTime.now())
                            .build();
                }

                countryRepository.save(country);
            }


//            After saving countries
            List<CountryEntity> allCountries = countryRepository.findAll();
            int totalCountries = allCountries.size();

//            Sort by estimated GDP descending
            List<CountryEntity> topFive = allCountries.stream()
                    .filter(c -> c.getEstimatedGdp() != null)
                    .sorted((a, b) -> Double.compare(b.getEstimatedGdp(), a.getEstimatedGdp()))
                    .limit(5)
                    .toList();
//            Create Image
            int width = 600, height = 400;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

//            Background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

//            Text Styling
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 18));

            int y =40;
            g.drawString("Country Summary", 20, y);
            y += 40;

            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.drawString("Total Countries: " + totalCountries, 20, y);
            y += 30;

            g.drawString("Top 5 Countries by Estimated GDP: ", 20, y);
            y += 25;

            for (CountryEntity c : topFive) {
                g.drawString("- " + c.getName() + " (" + String.format("%,.2f", c.getEstimatedGdp()) + ")", 40, y);
                y += 20;
            }

            y += 30;
            g.drawString("Last Refreshed: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 20, y);

            g.dispose();

//            Create cache folder if it doesn't exist
            File cacheDir = new File("cache");
            if (!cacheDir.exists()) cacheDir.mkdirs();

//            Save Image
            ImageIO.write(image, "png", new File("cache/summary.png"));

            return "Countries refreshed successfully";

        } catch (IOException e) {
            throw new RuntimeException("External data source unavailable: " + e.getMessage());
        }
    }



    public List<CountryEntity> getAllCountries(String region, String currency, String sort) {
        List<CountryEntity> countries = countryRepository.findAll();

//        Filtering
        if (region != null && !region.isEmpty()) {
            countries = countries.stream()
                    .filter(c -> region.equalsIgnoreCase(c.getRegion()))
                    .toList();
        }

        if (currency != null && !currency.isEmpty()) {
            countries = countries.stream()
                    .filter(c -> currency.equalsIgnoreCase(c.getCurrencyCode()))
                    .toList();
        }

//        Sorting
        if (sort != null) {
            switch (sort.toLowerCase()) {
                case "gdp_asc" -> countries = countries.stream()
                        .sorted(Comparator.comparing(CountryEntity::getEstimatedGdp,
                                Comparator.nullsLast(Double::compareTo)))
                        .toList();

                case "gdp_desc" -> countries = countries.stream()
                        .sorted(Comparator.comparing(CountryEntity::getEstimatedGdp,
                                Comparator.nullsLast(Double::compareTo))
                                .reversed())
                        .toList();
            }
        }
        return countries;
    }



    public CountryEntity getCountryByName(String name){
        return countryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NoSuchElementException("Country not found"));
    }

    public void deleteCountryByName(String name) {
        CountryEntity country = countryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NoSuchElementException("Country not found"));
        countryRepository.delete(country);
    }

    public Map<String, Object> getStatus() {
        long totalCountries = countryRepository.count();

        LocalDateTime lastRefreshAt = countryRepository.findAll().stream()
                .map(CountryEntity::getLastRefreshedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        Map<String, Object> status = new HashMap<>();
        status.put("total_countries", totalCountries);
        status.put("last_refreshed_at", lastRefreshAt);

        return status;
    }
}
