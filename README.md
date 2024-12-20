# Ecommerce-microservice-SEAL

## Table of Contents
1. [Sequence Diagram](#sequence-diagram)
    - [Choreography-based Saga](#choreography-based-saga)
    - [Orchestration-based Saga](#orchestration-based-saga)
2. [List of Technologies Used](#list-of-technologies-used)
3. [Prerequisite](#prerequisite)
4. [Setup](#setup)
5. [Running the App](#running-the-app)
    - [Adding to Host File](#adding-to-host-file)
    - [Run Command in Spring-Boot-Microservices Directory](#run-command-in-spring-boot-microservices-directory)
6. [Keycloak](#keycloak)
    - [Admin Console](#admin-console)
    - [Get Access Token](#get-access-token)
    - [Account](#account)
7. [Swagger](#swagger)
8. [Zipkin](#zipkin)
9. [ActiveMQ](#activemq)
10. [Apache Kafka](#apache-kafka)
## Sequence Diagram

#### Choreography-based Saga

```mermaid
sequenceDiagram
    participant Customer
    participant OrderService
    participant InventoryService
    participant PaymentService
    participant NotificationService

    Customer->>OrderService: Create Order
    OrderService->>OrderService: Emit OrderCreated Event
    OrderService->>Customer: Order Created

    OrderService-->>InventoryService: OrderCreated Event
    InventoryService->>InventoryService: Reserve Inventory
    InventoryService->>InventoryService: Emit InventoryReserved Event
    InventoryService-->>PaymentService: InventoryReserved Event

    PaymentService->>PaymentService: Process Payment
    PaymentService->>PaymentService: Emit PaymentProcessed Event
    PaymentService-->>NotificationService: PaymentProcessed Event

    NotificationService->>NotificationService: Send Confirmation Email
    NotificationService->>Customer: Order Confirmed

    Note right of OrderService: If any step fails

    InventoryService-->>OrderService: InventoryReserveFailed Event
    PaymentService-->>OrderService: PaymentFailed Event
    OrderService->>OrderService: Emit OrderFailed Event
    OrderService->>Customer: Order Failed
    OrderService-->>NotificationService: OrderFailed Event
    NotificationService->>NotificationService: Send Cancellation Email
    NotificationService->>Customer: Order Cancelled

    OrderService-->>InventoryService: OrderFailed Event
    InventoryService->>InventoryService: Compensate Inventory

    OrderService-->>PaymentService: OrderFailed Event
    PaymentService->>PaymentService: Compensate Payment

```

#### Orchestration-based Saga

```mermaid
sequenceDiagram
    participant Customer
    participant Orchestrator
    participant OrderService
    participant InventoryService
    participant PaymentService
    participant NotificationService

    Customer->>OrderService: Create Order
    OrderService->>Orchestrator: Order Created
    Orchestrator->>InventoryService: Reserve Inventory
    InventoryService->>Orchestrator: Inventory Reserved
    Orchestrator->>PaymentService: Process Payment
    PaymentService->>Orchestrator: Payment Processed
    Orchestrator->>OrderService: Confirm Order
    OrderService->>Customer: Order Confirmed
    Orchestrator->>NotificationService: Send Confirmation Email
    NotificationService->>Customer: Order Confirmation Sent

    Note right of Orchestrator: If any step fails

    Orchestrator->>PaymentService: Compensate Payment
    PaymentService->>Orchestrator: Payment Compensated
    Orchestrator->>InventoryService: Compensate Inventory
    InventoryService->>Orchestrator: Inventory Compensated
    Orchestrator->>OrderService: Cancel Order
    OrderService->>Customer: Order Cancelled
    Orchestrator->>NotificationService: Send Cancellation Email
    NotificationService->>Customer: Order Cancellation Sent


```

## List what has been used

- [Spring Boot](https://spring.io/projects/spring-boot), web framework, makes it easy to create stand-alone,
  production-grade Spring based Applications
- [AWS Cloud](https://aws.amazon.com/), using AWS SDK for Java 2.x
    - S3, SQS, etc
- [Apache Kafka](https://kafka.apache.org/), a distributed and fault-tolerant stream processing system used for
  event-driven communication between microservices.
- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix), a service discovery mechanism that
  allows microservices to find and communicate with each other without hard-coding the hostname and port.
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway), an api gateway that provide a simple, yet
  effective way to route to APIs and provide cross-cutting concerns to them such as:
  security, monitoring/metrics, and resiliency.
- [Spring Data Redis](https://docs.spring.io/spring-data/redis/reference/redis/redis-cache.html), provides an
  implementation of Spring Framework’s Cache Abstraction.
- [Spring Integration](https://docs.spring.io/spring-integration/reference/mqtt.html), provides inbound and outbound
  channel adapters to support the Message Queueing Telemetry Transport (MQTT) protocol.
- [Resilience4j](https://github.com/resilience4j/resilience4j), a library that helps prevent cascading failures and
  provides mechanisms for graceful degradation and self-healing when external services experience issues.
- [Zipkin](https://zipkin.io/), a distributed tracing system that provides end-to-end visibility into how requests flow
  through the system, helping troubleshoot issues in distributed architectures.
- ~~Spring Cloud Sleuth, autoconfiguration for distributed tracing~~
- [Micrometer Tracing](https://micrometer.io/docs/tracing) with Brave, library for distributed tracing (update to Spring
  Boot 3.x)
- [Docker](https://www.docker.com/) and docker-compose, for containerization
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
  provides repository support for the Jakarta Persistence API
- [Flywaydb](https://flywaydb.org/) for migrations
- [Keycloak](https://www.keycloak.org/) for providing authentication, user management, fine-grained authorization
- [PostgreSQL](https://www.postgresql.org/)

## Prerequisite

- Java 17
- Maven
- Docker
- GNU Make
- WSL (if using Windows)

## Setup

- Microservice repositories
    - spring-boot-microservices:https://github.com/haons211/Ecommerce-microservice-SEAL
        - shared configuration files, components, etc. that can be reused in other microservices (order-service,
          inventory-service, etc)
    - discovery-service : https://github.com/haons211/Ecommerce-microservice-SEAL/tree/main/discovery-service
        - This microservice acts as a registry for all the other microservices, allowing them to find and communicate
          with each other.
    - gateway-service : https://github.com/haons211/Ecommerce-microservice-SEAL/tree/main/gateway-service
        - This microservice acts as an entry point for external requests, routing them to the appropriate microservice.
    - user-service : https://github.com/haons211/Ecommerce-microservice-SEAL/tree/main/user-service
        - This microservice handles user-related functionality, such as creating, reading, and updating user data.
    - order-service: Updating
        - This microservice handles order-related functionality, such as creating, reading, and updating order data.
    - inventory-service: Updating
        - This microservice handles inventory-related functionality, such as managing product stock levels.
    - payment-service: Updating
        - This microservice handles payment-related functionality, such as processing payments.
    - notification-service: Updating
        - This microservice handles notification-related functionality, such as sending confirmation and cancellation
          emails.


```

## Running the app

##### Adding to host file

```bash
127.0.0.1   keycloak
```

##### Run command in spring-boot-microservices-eric6166 directory

- Docker environment

```bash
# Docker compose up
make up

# Docker compose down
make down
```

- Non Docker / standalone environment

```bash
# Docker compose up
make local-up

# Start discovery-service-eric6166, gateway-service-eric6166, user-service-eric6166

# Start microservice   

# Docker compose down
make local-down
```

#### Keycloak

##### Admin console

```bash
Non Docker / standalone environment: http://localhost:8090/

Docker environment: http://keycloak:8090/

username/password

admin/admin
```

![img_5.png](img_5.png)

##### Get access token

```bash
Non Docker / standalone environment: POST http://localhost:8090/realms/spring-boot-microservices-realm/protocol/openid-connect/token

Docker environment: POST http://keycloak:8090/realms/spring-boot-microservices-realm/protocol/openid-connect/token
#Basic Auth
Username="microservices-auth-client"
Password="123456789"
#form data
'grant_type="password"'
'scope="openid offline_access"'
'username="admin"'
'password="P@ssw0rd"'
```

- Account

```bash
username/password

admin/P@ssw0rd
customer/P@ssw0rd
guest/P@ssw0rd
```

##### Swagger

```bash
http://localhost:8181/swagger-ui.html
```



##### Zipkin

```bash
http://localhost:9411/
```


##### ActiveMQ

```bash
http://localhost:8161/

username/password

admin/admin
```



##### Apache Kafka

- Using [Offset Explorer](https://www.kafkatool.com) - a GUI application for managing and using Apache Kafka clusters

```bash
Cluster name: spring-boot-microservices
Zookeeper Host: localhost
Zookeeper Port: 2181

Bootstrap servers: localhost:9092
```

## How to Contribute

### Commit Message Conventions

To maintain a clear and consistent commit history, please follow the following commit message conventions:

**Type:**

  * **feat:** A new feature
  * **fix:** A bug fix
  * **docs:** Documentation only changes
  * **style:** Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc.)
  * **refactor:** A code change that neither fixes a bug nor adds a feature
  * **perf:** A code change that improves performance
  * **test:** Adding missing tests or correcting existing tests
  * **chore:** Changes to the build process or auxiliary tools and libraries   


**Scope:**

  * **component:** The specific component affected by the change
  * **feature:** The specific feature affected by the change

**Subject:**

  * A concise description of the change, in imperative mood.

**Body:**

  * A more detailed description of the change, if necessary.

**Footer:**

  * References to issues, pull requests, or other relevant information.

**Example:**

```bash
feat(api): Add endpoint for retrieving user data
```
### Contributing Workflow

### Contributing Workflow

1. **Fork the Repository:**
   - Fork the main repository to your personal GitHub account.

2. **Clone Your Fork:**
   - Clone your forked repository to your local machine:
     ```bash
     git clone 
     ```

3. **Create a New Branch:**
   - Create a new branch for your feature or bug fix:
     ```bash
     git checkout -b feature-name
     ```

4. **Make Your Changes:**
   - Make the necessary changes   
 to the code.

5. **Commit Your Changes:**
   - Stage your changes:
     ```bash
     git add .
     ```
   - Commit your changes with a clear and concise message following the specified conventions:
     ```bash
     git commit -m "feat(api): Add endpoint for retrieving user data"
     ```

6. **Push Your Changes to Your Fork:**
   - Push your branch to your remote repository:
     ```bash
     git push origin feature-name
     ```

7. **Create a Pull Request:**
   - Go to your forked repository on GitHub.
   - Click the "New pull request" button.
   - Select your feature branch as the source branch and the main branch as the target branch.
   - Provide a clear and concise description of your changes.
   - Provide a comment :```/gemini-review ``` . For reviewing new pull request.
   - Submit your pull request.

8. **Review and Merge:**
   - Your pull request will be reviewed by the project maintainers.
   - If approved, your changes will be merged into the main branch. 

