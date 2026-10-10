# Flight Booking System

A full-stack flight booking platform built with microservices, designed to handle user authentication, flight search, booking, payment processing, ticket generation, and email notifications.

## Overview

This project demonstrates a modern distributed system using Spring Boot microservices, an API gateway, service discovery, MySQL persistence, and a React frontend. It allows users to browse available flights, create bookings, process payments, and receive booking confirmations.

## Architecture

The system is composed of the following services:

- API Gateway: central entry point for client requests
- Eureka Server: service discovery and registry
- Auth Service: user registration, login, JWT generation, and security
- Flight Service: flight data and availability management
- Booking Service: booking creation and order workflow
- Payment Service: payment integration and transaction handling
- Ticket Service: ticket issuance and booking confirmation
- Notification Service: e-mail and notification delivery
- Frontend: React application for users to interact with the platform
- MySQL: database for application data

A high-level flow looks like this:

```text
Client -> Frontend -> API Gateway -> Microservices -> MySQL -> Eureka Server for discovery
```

## Features

- User registration and login
- JWT-based authentication and authorization
- Flight discovery and search flow
- Booking lifecycle management
- Payment processing using Stripe and Razorpay integrations
- Ticket generation and booking confirmation
- Email notifications for user actions
- Docker-based local deployment
- Microservice architecture with Eureka and gateway routing

## Tech Stack

### Backend
- Java 21
- Spring Boot 3/4
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Security
- Spring Data JPA
- MySQL
- JWT (jjwt)
- Docker

### Frontend
- React
- Vite
- Tailwind CSS
- Axios
- React Router

### Integrations
- Stripe
- Duffel API
- JavaMail

## Repository Structure

```text
Flight_Booking_System/
├── .env.example
├── .dockerignore
├── .gitignore
├── Dockerfile.frontend
├── Dockerfile.spring-service
├── docker-compose.yml
├── api-gateway/
├── auth-service/
├── booking-service/
├── eureka-server/
├── flight-service/
├── frontend/
├── notification-service/
├── payment-service/
├── ticket-service/
├── README.md
└── ...
```

## Prerequisites

Before running the project, make sure you have:

- Java 21+
- Maven
- Node.js 22+
- npm
- Docker and Docker Compose
- MySQL (if running services manually)

## Configuration

Copy the sample environment file and update the values:

```bash
cp .env.example .env
```

The project expects the following environment variables:

```env
MYSQL_ROOT_PASSWORD=change_me
DB_USERNAME=change_me
DB_PASSWORD=change_me
DUFFEL_ACCESS_TOKEN=replace_me
STRIPE_SECRET_KEY=replace_me
STRIPE_WEBHOOK_SECRET=replace_me
RAZORPAY_KEY_ID=replace_me
RAZORPAY_KEY_SECRET=replace_me
MAIL_USERNAME=replace_me
MAIL_PASSWORD=replace_me
```

## Run with Docker Compose

From the project root:

```bash
docker compose up --build
```

This starts the following services:

- Eureka Server: http://localhost:8761
- API Gateway: http://localhost:8081
- Auth Service: http://localhost:8082
- Flight Service: http://localhost:8083
- Booking Service: http://localhost:8084
- Payment Service: http://localhost:8085
- Ticket Service: http://localhost:8086
- Notification Service: http://localhost:8087
- MySQL: localhost:3306

To stop the stack:

```bash
docker compose down
```

## Run Frontend Separately

```bash
cd frontend
npm install
npm run dev
```

The frontend will run on:

- http://localhost:5173

## Run Backend Services Manually

Each Spring Boot service is a separate Maven project. Example:

```bash
cd eureka-server
./mvnw spring-boot:run
```

Then in another terminal:

```bash
cd api-gateway
./mvnw spring-boot:run
```

Repeat for the required backend services:

```bash
cd auth-service
./mvnw spring-boot:run
cd ../flight-service
./mvnw spring-boot:run
cd ../booking-service
./mvnw spring-boot:run
cd ../payment-service
./mvnw spring-boot:run
cd ../ticket-service
./mvnw spring-boot:run
cd ../notification-service
./mvnw spring-boot:run
```

## Production Notes

- Configure real credentials for payment gateways and email providers.
- Use secure secret management for production deployments.
- Update database and service URLs based on your deployment environment.
- Add proper ingress, reverse proxy, and monitoring for production use.

## License

This project is currently distributed without an explicit license file.

## Contributing

Contributions, bug reports, and feature requests are welcome. Please open an issue or submit a pull request with a clean description of your changes.

## Contact

For questions or project discussions, reach out through the repository owner or the GitHub project page.
