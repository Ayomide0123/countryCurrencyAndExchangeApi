package com.hng.countryCurrencyAndExchangeApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CountryCurrencyAndExchangeApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CountryCurrencyAndExchangeApiApplication.class, args);
	}

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(600000); // 10 minutes
        factory.setReadTimeout(600000);    // 10 minutes
        return new RestTemplate(factory);
    }

}
