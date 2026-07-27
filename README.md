# StockPilot — Multi-Tenant Inventory Management System

A production-ready Spring Boot backend for managing inventory, sales, invoicing, and analytics across multiple companies with role-based access control.

---

## Features

- **Multi-Tenant Architecture** — Platform supports multiple companies with complete data isolation
- **RBAC** — Four roles: `PLATFORM_ADMIN`, `COMPANY_ADMIN`, `SALESPERSON`, `VIEWER`
- **Auto-Invoicing** — Every sale automatically generates a numbered invoice with full company/customer details
- **Inventory Tracking** — Real-time stock with low-stock/out-of-stock alerts and movement history
- **Sales Management** — Record sales, deduct stock, calculate taxes, and track payment methods
- **Purchase Orders** — Create POs, receive goods, and auto-update stock levels
- **Dashboard Analytics** — Revenue, top products, sales trends, category breakdown, top salespeople
- **JWT Authentication** — Access + refresh token flow with secure password hashing
- **Swagger/OpenAPI** — Full API docs at `/api/swagger-ui.html`

---

## Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Runtime     | Java 17, Spring Boot 3.2.5          |
| Database    | PostgreSQL 16                       |
| Security    | Spring Security + JWT (jjwt 0.12.5) |
| Cache       | Caffeine                            |
| API Docs    | springdoc-openapi 2.5.0             |
| Build       | Maven                               |
| Container   | Docker + Docker Compose             |

---

## Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+**
- **PostgreSQL 16** (or use Docker)

---

## Quick Start

### Option A: Docker Compose (Recommended)

```bash
# Clone the project
cd stockpilot-inventory

# Start PostgreSQL + App
docker compose up -d

# App runs at: http://localhost:8080/api
# Swagger UI:  http://localhost:8080/api/swagger-ui.html
```

### Option B: Local Development

```bash
# 1. Create PostgreSQL database
psql -U postgres
CREATE DATABASE stockpilot_db;
\q

# 2. Run the application
cd stockpilot-inventory
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or with custom DB credentials:
DB_USERNAME=postgres DB_PASSWORD=yourpass ./mvnw spring-boot:run
```

---

## Default Credentials

On first startup, the system auto-creates:

| Field    | Value                |
|----------|----------------------|
| Email    | admin@stockpilot.com |
| Password | Admin@123456         |
| Role     | PLATFORM_ADMIN       |

---

## API Endpoints

### Authentication
| Method | Endpoint              | Access  | Description            |
|--------|-----------------------|---------|------------------------|
| POST   | `/api/auth/login`     | Public  | Login & get JWT tokens |
| POST   | `/api/auth/register`  | Public  | Register new user      |
| POST   | `/api/auth/refresh`   | Public  | Refresh access token   |
| POST   | `/api/auth/logout`    | Auth    | Logout & revoke tokens |

### Companies (Platform Admin)
| Method | Endpoint                              | Description           |
|--------|---------------------------------------|-----------------------|
| POST   | `/api/companies`                      | Create company        |
| GET    | `/api/companies`                      | List all companies    |
| GET    | `/api/companies/{id}`                 | Get company details   |
| PUT    | `/api/companies/{id}`                 | Update company        |
| PATCH  | `/api/companies/{id}/toggle-active`   | Enable/disable        |

### Users
| Method | Endpoint                          | Access          | Description        |
|--------|-----------------------------------|-----------------|--------------------|
| POST   | `/api/users`                      | Admin           | Create user        |
| GET    | `/api/users?companyId=X`          | Admin           | List company users |
| GET    | `/api/users/{id}`                 | Auth            | Get user details   |
| PUT    | `/api/users/{id}`                 | Admin           | Update user        |
| PATCH  | `/api/users/{id}/toggle-active`   | Admin           | Enable/disable     |

### Products
| Method | Endpoint                               | Access  | Description         |
|--------|----------------------------------------|---------|---------------------|
| POST   | `/api/products`                        | Admin   | Create product      |
| GET    | `/api/products`                        | Auth    | List products       |
| GET    | `/api/products/{id}`                   | Auth    | Product details     |
| PUT    | `/api/products/{id}`                   | Admin   | Update product      |
| PATCH  | `/api/products/{id}/toggle-active`     | Admin   | Enable/disable      |
| GET    | `/api/products/low-stock`              | Auth    | Low stock list      |

### Sales (Auto-generates Invoice)
| Method | Endpoint           | Access         | Description    |
|--------|--------------------|----------------|----------------|
| POST   | `/api/sales`       | Admin/Sales    | Create sale    |
| GET    | `/api/sales`       | Auth           | List sales     |
| GET    | `/api/sales/{id}`  | Auth           | Sale details   |

### Invoices
| Method | Endpoint                           | Description       |
|--------|------------------------------------|-------------------|
| GET    | `/api/invoices`                    | List invoices     |
| GET    | `/api/invoices/{id}`               | Invoice details   |
| PATCH  | `/api/invoices/{id}/mark-paid`     | Mark as paid      |

### Stock Management
| Method | Endpoint                                   | Description            |
|--------|--------------------------------------------|------------------------|
| POST   | `/api/stock/adjust`                        | Manual stock adjust    |
| GET    | `/api/stock/movements`                     | All movements          |
| GET    | `/api/stock/movements/product/{productId}` | Product movements      |

### Purchase Orders
| Method | Endpoint                              | Description            |
|--------|---------------------------------------|------------------------|
| POST   | `/api/purchase-orders`                | Create PO              |
| GET    | `/api/purchase-orders`                | List POs               |
| GET    | `/api/purchase-orders/{id}`           | PO details             |
| POST   | `/api/purchase-orders/{id}/receive`   | Receive & update stock |

### Categories, Customers, Suppliers, Dashboard, Notifications, Profile
Similar RESTful CRUD patterns — see Swagger UI for full documentation.

---

## Sale → Invoice Flow

```
POST /api/sales
   ├── Validates stock availability for each item
   ├── Deducts quantity from products
   ├── Records StockMovement (type: SALE)
   ├── Creates Sale with sale number (SALE-YYYYMMDD-XXXXX)
   ├── AUTO-GENERATES Invoice (PREFIX-YYMM-XXXXXX)
   │     └── Snapshots company name, address, GSTIN
   │     └── Snapshots customer details
   ├── Sends low-stock notifications to Company Admins
   └── Returns SaleResponse with invoiceId & invoiceNumber
```

---

## Project Structure

```
src/main/java/com/stockpilot/inventory/
├── StockPilotApplication.java
├── audit/          # BaseEntity, AuditConfig
├── config/         # CORS, Swagger, Cache, DataInitializer
├── controller/     # 14 REST controllers
├── dto/            # Request/Response DTOs by domain
├── entity/         # 15 JPA entities
├── enums/          # 7 enums
├── exception/      # Global exception handler
├── repository/     # 14 JPA repositories
├── security/       # JWT auth, RBAC, filters
├── service/        # 12 service classes
└── util/           # ApiRoutes, Constants, SlugGenerator
```

---

## Environment Variables

| Variable              | Default              | Description             |
|-----------------------|----------------------|-------------------------|
| `DB_USERNAME`         | postgres             | Database username       |
| `DB_PASSWORD`         | postgres             | Database password       |
| `JWT_SECRET`          | (built-in)           | JWT signing key         |
| `SPRING_PROFILES_ACTIVE` | dev               | Active Spring profile   |

---

## License

MIT
