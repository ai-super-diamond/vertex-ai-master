# Clean Architecture Implementation

This document describes the Clean Architecture implementation for the Vertex AI Master CLI project.

## Architecture Layers

### 1. Domain Layer (Innermost)
- **Entities**: Business objects with core business rules
  - `Model` - Represents AI models with alias and full name
  - `Region` - Represents geographic regions for model deployment
- **Repository Interfaces**: Abstract data access operations
  - `ModelRepository` - Operations for model data access
  - `RegionRepository` - Operations for region data access
- **Service Interfaces**: Business logic operations
  - `ModelResolutionService` - Logic for model alias resolution

### 2. Application Layer (Business Rules)
- **Use Cases**: Core business operations
  - `GenerateContentUseCase` - Content generation business logic
  - `CheckRegionAvailabilityUseCase` - Region availability checking
- **DTOs**: Data Transfer Objects for use case communication
  - `GenerateContentRequest` - Request object for content generation

### 3. Interface Adapters Layer
- **Controllers**: API boundary interfaces
  - `ModelController` - Interface for model operations
- **Presenters**: Output formatting
  - `GenerateContentPresenter` - Formats responses and errors
- **Implementations**: Adapter implementations
  - `ModelControllerImpl` - Concrete controller implementation

### 4. Infrastructure Layer (Outermost)
- **Frameworks & Drivers**: External service implementations
  - `GoogleVertexAIAdapter` - Google Vertex AI API communication
- **Repository Implementations**: Concrete data access implementations
  - `ModelRepositoryImpl` - In-memory model storage implementation
- **Configuration**: Dependency injection module
  - `ApplicationModule` - Configuration for dependency injection

## Dependency Rules

The dependency rule states that inner circles should not know about outer circles. In this implementation:
- Domain layer interfaces are implemented by infrastructure layer
- Application layer depends on domain layer interfaces
- Interface adapters depend on application layer use cases
- Infrastructure implements domain interfaces and depends on external frameworks

## Benefits

This architecture provides:
- Independence from frameworks
- Testable business logic
- Independence from UI and database
- Business rules at the center
- Clear separation of concerns
- Maintainable codebase
`
