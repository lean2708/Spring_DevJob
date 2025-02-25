# Spring Dev Job
Spring Dev Job is a Restful API service developed with Spring Boot, providing IT recruitment management functionalities.
## Tech Stack
- **Build Tool:**: Maven >= 3.4.1
- **Java**: 17
- **Framework**: Spring Boot 3.2.x
- **Database**: MySQL
## Build and Run
**1. Clone Repository :**
```java
git clone https://github.com/lean2708/Spring_DevJob.git  
cd Spring_DevJob
```
- Then, create and configure the **.env** file

## 2. Docker Guideline
**Build Image**
```java
docker build -t lean2708/spring-devjob:0.0.1 .
```
**Run Your Application**
```java
docker-compose up -d
```
**3. Swagger Documentation Guide (API Document):**
- **After running the application, you can access Swagger UI at:**
```java
http://localhost:8080/devjob/swagger-ui/index.html
```
- The default URL to retrieve the API documentation in JSON format **(Explore section)**:
```java
/devjob/v3/api-docs
```
**4. Sample VNPAY Payment Information (Test):**
| Bank                  | NCB                      |
|-----------------------|--------------------------|
| Card Number           | 9704198526191432198      |
| Cardholder Name       | NGUYEN VAN A             |
| Issue Date            | 07/15                    |
| OTP Password          | 123456                   |
