# E-Commerce Shipping Charge Estimator
### Assignment Submission — Jumbotail

---

## About the Project
This is a Spring Boot REST API application that calculates shipping charges
for a B2B e-commerce marketplace. Sellers drop products at the nearest
warehouse, and the system calculates the shipping cost to deliver to the
customer based on distance, weight, and delivery speed.

---

## Tech Stack
- **Java 21** + **Spring Boot**
- **PostgreSQL** — primary database
- **Spring Data JPA** + **Hibernate** — ORM
- **Caffeine Cache** — response caching via @Cacheable
- **Lombok** — boilerplate reduction
- **JUnit 5** + **Mockito** + **AssertJ** — unit testing

---

## How to Run

### Prerequisites
- Java 21+
- PostgreSQL running locally
- Maven (or use included ./mvnw)

### Setup
1. Create a PostgreSQL database
2. Update `src/main/resources/application.yaml`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```
3. Run the application:
```bash
./mvnw spring-boot:run
```

---

## APIs

### 1. Get Nearest Warehouse for a Seller
```
GET /api/v1/warehouse/nearest?sellerId=123&productId=456
```
```json
{
  "warehouseId": 789,
  "lat": 12.99999,
  "lng": 37.923273
}
```

### 2. Get Shipping Charge from Warehouse to Customer
```
GET /api/v1/shipping-charge?warehouseId=789&customerId=456&deliverySpeed=standard
```
```json
{
  "shippingCharge": 150.00
}
```

### 3. Calculate Full Shipping (Seller → Nearest Warehouse → Customer)
```
POST /api/v1/shipping-charge/calculate
```
```json
{
  "sellerId": 123,
  "customerId": 456,
  "deliverySpeed": "express"
}
```
```json
{
  "shippingCharge": 180.00,
  "nearestWarehouse": {
    "warehouseId": 789,
    "lat": 12.99999,
    "lng": 37.923273
  }
}
```

---

## Shipping Logic

| Transport Mode | Distance    | Rate             |
|----------------|-------------|------------------|
| Aeroplane      | 500km+      | ₹1 per km per kg |
| Truck          | 100–499km   | ₹2 per km per kg |
| Mini Van       | 0–99km      | ₹3 per km per kg |

| Delivery Speed | Charge |
|----------------|--------|
| Standard       | ₹10 base + shipping charge |
| Express        | ₹10 base + ₹1.2/kg extra + shipping charge |

Distance is calculated using the **Haversine formula**.

---

## Unit Tests
```bash
./mvnw test
```
```
Tests run: 61, Failures: 0, Errors: 0, Skipped: 1
BUILD SUCCESS
```

| Test Class                  | Tests | What's covered                        |
|-----------------------------|-------|---------------------------------------|
| DistanceCalculatorTest      |  4    | Haversine formula, symmetry, edge cases |
| TransportModeSelectorTest   | 12    | All thresholds + boundary values      |
| ShippingChargeCalculatorTest| 12    | Standard, express, zero distance      |
| WarehouseUtilsTest          |  6    | Nearest pick, empty list, order independence |
| WarehouseServiceImplTest    |  9    | Mocked repos, exceptions, interactions |
| ShippingServiceImplTest     | 17    | Full flow, all 4 repos, all exceptions |

---

## Design Decisions

- **Haversine formula** to calculate real-world distance between coordinates
- **Strategy pattern** for transport mode selection based on distance
- **Builder pattern** across all entities and DTOs (via Lombok)
- **@Cacheable** on nearest warehouse and shipping charge endpoints
  to avoid repeated DB + calculation hits for same inputs
- **Layered architecture** — Controller → Service → Repository → Utils
- **Global exception handling** for missing sellers, customers,
  warehouses, and products with meaningful error messages

---

*Thank you for the opportunity. Looking forward to your feedback!*