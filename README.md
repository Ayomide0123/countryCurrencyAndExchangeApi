# Country Data Caching API

A RESTful API built with **Java Spring Boot** that fetches country data from an external API ([RESTCountries](https://restcountries.com/v2/all?fields=name,capital,region,population,flag,currencies)), combines it with exchange rates from the [Open Exchange Rate API](https://open.er-api.com/v6/latest/USD), and caches it in a MySQL database.

Each country record stores key information such as population, region, currency, exchange rate, and a computed **estimated GDP** value.  
The data can be refreshed, queried, filtered, sorted, or deleted via REST endpoints.

---

## Features

- **POST /countries/refresh** → Fetch & cache all country data (with GDP computation)
- **GET /countries** → Retrieve countries with optional filters & sorting  
  `?region=Africa` or `?currency=NGN` or `?sort=gdp_desc`
- **GET /countries/{name}** → Get one country by name
- **DELETE /countries/{name}** → Delete a country
- **GET /status** → View total countries and last refresh timestamp
- **GET /countries/image** → Serve generated summary image

During each refresh, the app also generates an image summary (`cache/summary.png`) showing:
- Total number of countries  
- Top 5 countries by estimated GDP  
- Last refresh timestamp  

---

## Tech Stack

- **Backend:** Java 17+, Spring Boot 3+  
- **Database:** MySQL  
- **HTTP Client:** `RestTemplate`  
- **JSON Parser:** Jackson (`ObjectMapper`)  
- **Build Tool:** Maven  

---

## Setup Instructions

### 1️. Clone the Repository

```bash
git clone https://github.com/Ayomide0123/countryCurrencyAndExchangeApi.git
cd countryCurrencyAndExchangeApi
```

### 2️. Configure MySQL Database

Create a new database, e.g. `country_db`, in MySQL:

```sql
CREATE DATABASE country_db;
```

Then update your `src/main/resources/application.properties` (or `.yml`) file:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/country_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### 3. Build the Project

Run this in your project root:

```bash
mvn clean install
```

This will compile the project and download all dependencies.

### 4️. Run the Application

Run using Maven:

```bash
mvn spring-boot:run
```

or directly from your IDE, run the `CountryCurrencyAndExchangeApiApplication.java` class.

The app starts at: **[http://localhost:8080](http://localhost:8080)**

---

## Estimated GDP Formula

```
estimated_gdp = (population × random(1000–2000)) ÷ exchange_rate
```

- Each refresh generates a new random multiplier per country.
- If a country’s exchange rate or currency is missing, `estimated_gdp` is set to `0` or `null`.

---

## Error Responses

| HTTP Code | Example Response | Description |
|------------|------------------|--------------|
| **400** | `{ "error": "Validation failed", "details": {"currency_code": "is required"} }` | Missing required fields |
| **404** | `{ "error": "Country not found" }` | Country name not found in DB |
| **503** | `{ "error": "External data source unavailable", "details": "Could not fetch data from RESTCountries API" }` | External API failed |
| **500** | `{ "error": "Internal server error" }` | Generic server failure |

---

## Environment Variables

Create a `.env` file in the project root:

| Variable           | Description                            | Default                                     |
| ------------------ | -------------------------------------- |---------------------------------------------|
| `DB_URL`      | JDBC connection string for your database                    | `jdbc:postgresql://localhost:5432/stringdb` |
| `DB_USERNAME`          | Database username | `mysql`                                     |
| `DB_PASSWORD` | Database password   | `password123`                                    |

---

## Author

**Oyetimehin Ayomide**
* 📧 [oyetimehin31@gmail.com](mailto:oyetimehin31@gmail.com)
* 💻 Backend Stack: Java / Spring Boot
