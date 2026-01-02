# Developer Onboarding - Clean Architecture

This document provides onboarding information for developers joining the Vertex AI Master CLI project, focusing on the Clean Architecture implementation.

## Overview

The project follows Clean Architecture principles with clear separation of concerns:

- **Domain Layer**: Core business logic and entities
- **Application Layer**: Use cases and application business rules
- **Interface Adapters**: Presenters, controllers, and converters
- **Infrastructure**: External frameworks, databases, and implementations

## Key Concepts

### Dependency Rule
Inner circles should not know about outer circles. Dependencies point inward toward the center.

### Architecture Layers

#### Domain Layer
- Contains business entities (`Model`, `Region`)
- Defines repository interfaces (`ModelRepository`, `RegionRepository`)
- Contains domain services (`ModelResolutionService`)
- Independent of external frameworks

#### Application Layer
- Contains use cases (`GenerateContentUseCase`, `CheckRegionAvailabilityUseCase`)
- Defines DTOs for data transfer between layers
- Depends on domain layer interfaces
- Contains application business rules

#### Interface Adapters Layer
- Contains controller interfaces and implementations
- Presenters for output formatting
- Converts between application and external interfaces
- Depends on application layer use cases

#### Infrastructure Layer
- Implements domain interfaces
- Contains external framework implementations
- Database implementations
- Configuration and dependency injection modules

## Getting Started

### Project Structure
```
src/
├── main/
│   ├── java/
│   │   ├── com.jguru.vertexai/
│   │   │   ├── domain/          # Domain layer
│   │   │   ├── application/     # Application layer
│   │   │   ├── adapter/         # Interface adapters
│   │   │   ├── infrastructure/  # Infrastructure layer
│   │   │   └── config/          # Configuration
│   │   └── resources/
│   └── test/
│       └── java/
└── test/
    └── java/
```

### Adding New Features

1. **Start with Domain Layer**: Define entities and interfaces
2. **Move to Application Layer**: Create use cases and DTOs
3. **Create Interface Adapters**: Build controllers and presenters
4. **Implement Infrastructure**: Create framework implementations
5. **Configure Dependencies**: Update the ApplicationModule

### Testing Strategy

- **Unit Tests**: Focus on domain entities and services
- **Integration Tests**: Validate layer-to-layer communication
- **Contract Tests**: Verify interface implementations
- **End-to-End Tests**: Validate complete architecture flow

### Dependency Injection

The `ApplicationModule` class manages dependency injection:

```java
ApplicationModule module = new ApplicationModule();
ModelController controller = module.provideModelController(
    module.provideGenerateContentUseCase(
        module.provideModelRepository(),
        module.provideModelResolutionService(module.provideModelRepository())
    )
);
```

### Common Patterns

- Repository pattern for data access
- Use case pattern for business operations
- Adapter pattern for external framework integration
- Presenter pattern for output formatting

## Architecture Validation

The project includes automated validation to ensure Clean Architecture principles are maintained:

- Tests verify dependency direction
- Integration tests validate complete flow
- Contract tests ensure interface compliance

## Troubleshooting

### Common Issues
- Dependencies pointing in wrong direction (outer circle depending on inner)
- Domain layer importing external framework classes
- Infrastructure implementations directly referenced in application layer

### Solutions
- Use interfaces to maintain proper abstraction
- Apply dependency inversion principle
- Ensure domain layer remains independent