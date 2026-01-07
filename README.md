# 🏦 FinTrack - Personal Finance Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)

[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-blue.svg)](https://spring.io/projects/spring-cloud)

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.java.com/)

[![Microservices](https://img.shields.io/badge/Architecture-Microservices-purple.svg)](https://microservices.io/)

[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A modern personal finance management system built with Spring Boot microservices architecture. Manage your budgets, track transactions, and get real-time alerts when you're about to exceed your spending limits.

## ✨ Features

### 🔐 **Authentication Service**
- User registration and login with JWT tokens
- Secure password encryption with BCrypt
- Token validation and user management

### 💰 **Transaction Service**
- Create, read, update, and delete transactions
- Categorize transactions (Food, Transport, Entertainment, Bills, Income)
- Calculate user balance automatically
- Transaction history and summaries

### 📊 **Budget Service**
- Create and manage monthly/weekly/yearly budgets
- Set spending limits by category
- Real-time budget tracking
- Automatic budget alerts and warnings

### ⚡ **Real-Time Integration**
- Automatic budget checking on every transaction
- Instant alerts when exceeding 80% of budget
- Critical alerts when budget is exceeded
- Console notifications with detailed spending analytics

🚀 Architecture & Technical Design

- API Gateway: Spring Cloud Gateway (routing & load balancing)
- Service Discovery: Netflix Eureka Server
- Inter-service Communication: OpenFeign with fallback mechanisms
- Load Balancing: Spring Cloud LoadBalancer
- Fault Tolerance: Resilience4j (circuit breaker pattern)

🏗️ System Architecture
- API Gateway (8765)
Acts as the single entry point for all client requests, handling routing and load balancing.

- Auth Service (8081)
Responsible for authentication, authorization, and JWT token management.

- Transaction Service (8082)
Manages user income and expense transactions and triggers budget validation.

- Budget Service (8085)
Handles budget creation, tracking, and spending limit validation.

- Eureka Server (8761)
Provides service registration and discovery, enabling dynamic communication between services.

Each microservice registers itself with the Eureka Server and communicates through the API Gateway.

