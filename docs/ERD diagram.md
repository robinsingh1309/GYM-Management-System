# FitManager Architecture Diagrams

This document represents the existing FitManager implementation. It describes the relational data model and the high-level relationships between application components without proposing an architectural redesign.

## Entity Relationship Diagram

```mermaid
erDiagram
    USERS {
        bigint id PK
        varchar email UK
        varchar password "nullable"
        varchar provider
        varchar provider_subject "nullable"
        enum role
        boolean active
        datetime created_at
        datetime updated_at
    }

    MEMBERS {
        bigint id PK
        varchar name
        varchar email UK
        varchar phone_number UK
        date date_of_birth "nullable"
        varchar gender "nullable"
        varchar address "nullable"
        date joining_date
        boolean active
        datetime created_at
        datetime updated_at
    }

    MEMBERSHIPS {
        bigint id PK
        bigint member_id FK
        enum membership_type
        date start_date
        date end_date
        decimal amount
        boolean active
        datetime created_at
        datetime updated_at
    }

    MEMBERSHIP_PRICING {
        bigint id PK
        enum membership_type
        decimal price
        boolean active
        datetime created_at
        datetime updated_at
    }

    PAYMENTS {
        bigint id PK
        bigint member_id FK
        bigint membership_id FK
        decimal amount
        date payment_date
        enum payment_mode
        datetime created_at
        datetime updated_at
    }

    TRAINERS {
        bigint id PK
        varchar name
        varchar email UK
        varchar phone_number UK
        date joining_date
        integer experience_years "nullable"
        bigint user_id FK "nullable and unique"
        boolean active
        boolean deleted
        datetime created_at
        datetime updated_at
    }

    TRAINER_SPECIALIZATIONS {
        bigint trainer_id FK
        enum specialization
    }

    MEMBERS ||--o{ MEMBERSHIPS : "has"
    MEMBERS ||--o{ PAYMENTS : "makes"
    MEMBERSHIPS ||--o{ PAYMENTS : "receives"
    USERS o|--o| TRAINERS : "optionally linked"
    TRAINERS ||--o{ TRAINER_SPECIALIZATIONS : "has"
```

### ERD Notes

- `BaseEntity` is a mapped superclass providing common identifier and timestamp fields. It does not have its own table.
- `Membership` owns its relationship to `Member` through `member_id`.
- `Payment` owns its relationships to both `Member` and `Membership`.
- `Trainer` optionally owns a one-to-one association with `User` through a nullable, unique `user_id`.
- Trainer specializations are stored in an element-collection table. They are not represented by a separate JPA entity.
- `MembershipPricing` has no database foreign-key relationship with `Membership`. They are associated through the `MembershipType` business value.
- `Exercise` is not a persisted JPA entity. Exercise data is loaded from JSON and therefore does not belong in the relational ERD.
- Qdrant collections are excluded because they are vector-storage structures rather than relational database tables.

## High-Level Component Relationship Diagram

```mermaid
flowchart LR
    User["Application User"]

    subgraph Frontend["React Frontend"]
        UI["Pages and Components"]
        AuthState["Authentication Context"]
        APIClient["Shared Axios Client"]
    end

    subgraph Backend["Spring Boot Backend"]
        Security["Spring Security<br/>JWT and Google OAuth"]
        Controllers["REST Controllers"]
        DTOs["Request and Response DTOs"]
        Services["Service Interfaces"]
        Implementations["ServiceImpl Classes"]
        Mapper["Mappers"]
        Repositories["Spring Data Repositories"]
    end

    subgraph ApplicationData["Application Data"]
        MySQL[("MySQL")]
        ExerciseData["Exercise JSON and<br/>In-Memory Catalog"]
        PDFs["PDF Knowledge Base"]
    end

    subgraph AIIntegrations["AI and Vector Integrations"]
        HuggingFace["Hugging Face<br/>Embeddings"]
        Qdrant[("Qdrant Vector Database")]
        Groq["Groq Language Model"]
    end

    Google["Google OIDC"]

    User --> UI
    UI --> AuthState
    UI --> APIClient

    APIClient --> Security
    Security --> Controllers
    Security <--> Google

    Controllers <--> DTOs
    Controllers --> Services
    Services --> Implementations

    Implementations --> Mapper
    Mapper <--> DTOs

    Implementations --> Repositories
    Repositories --> MySQL

    Implementations --> ExerciseData
    Implementations --> PDFs
    Implementations --> HuggingFace
    Implementations --> Qdrant
    Implementations --> Groq
```

### Component Notes

- The frontend communicates with the backend through the existing shared Axios client.
- Authentication state is managed within the frontend authentication context.
- Spring Security protects backend requests and handles JWT and Google OAuth/OIDC authentication.
- Controllers define the HTTP boundary and delegate work through service interfaces.
- Each service interface is implemented by its corresponding `ServiceImpl`, preserving the project's established separation of concerns.
- Service implementations coordinate business logic, mapping, repositories, and integrations.
- Repositories provide persistence access to MySQL.
- Exercise recommendations use the JSON-backed exercise catalogue and vector-related integrations.
- The personal assistant uses PDF knowledge, embeddings, Qdrant retrieval, and the Groq language model.
- Individual controllers, services, repositories, DTOs, and methods are intentionally omitted to keep this an architectural overview rather than a class-level dependency diagram.
