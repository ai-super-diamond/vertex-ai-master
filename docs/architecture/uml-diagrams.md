# UML Diagrams for Clean Architecture

This document contains UML diagrams showing the Clean Architecture structure of the Vertex AI Master CLI project.

## Package Structure

```
+---------------------------------------+
|                Infrastructure         |
|  +-------------------+                |
|  |GoogleVertexAIAdapter|               |
|  |ModelRepositoryImpl |               |
|  +-------------------+                |
|  |Implementation      |               |
|  |of domain interfaces|              |
+---------------------------------------+
                     |
                     | (implements)
                     |
+---------------------------------------+
|                Interface Adapters     |
|  +-------------------+                |
|  |   ModelController  |                |
|  |GenerateContentPresenter|            |
|  +-------------------+                |
|  |ControllerImpl      |                |
|  |Adapter for UI/API  |                |
+---------------------------------------+
                     |
                     | (depends on)
                     |
+---------------------------------------+
|               Application Layer       |
|  +-------------------+                |
|  |GenerateContentUseCase|             |
|  |CheckRegionUseCase  |               |
|  +-------------------+                |
|  |Use cases with      |                |
|  |application business|                |
|  |rules               |                |
+---------------------------------------+
                     |
                     | (depends on)
                     |
+---------------------------------------+
|                Domain Layer           |
|  +-------------------+                |
|  |      Model        |                |
|  |      Region       |                |
|  | ModelRepository   |                |
|  |ModelResolutionService|             |
|  +-------------------+                |
|  |Entities and        |                |
|  |interfaces          |                |
+---------------------------------------+
```

## Class Diagram - Model Repository

```
┌─────────────────────────────────────┐
│         Domain Layer                │
├─────────────────────────────────────┤
│ «interface»                         │
│ ModelRepository                     │
├─────────────────────────────────────┤
│ + findByAlias(String): Model       │
│ + existsByAlias(String): boolean   │
└─────────────────────────────────────┘
           ▲ implements
┌─────────────────────────────────────┐
│      Infrastructure Layer           │
├─────────────────────────────────────┤
│ ModelRepositoryImpl                 │
├─────────────────────────────────────┤
│ - models: Map<String, Model>        │
├─────────────────────────────────────┤
│ + findByAlias(String): Model       │
│ + existsByAlias(String): boolean   │
└─────────────────────────────────────┘
```

## Sequence Diagram - Content Generation Flow

```
User → ModelController: generateContent(modelAlias, prompt)
ModelController → GenerateContentPresenter: presentSuccess() / presentError()
GenerateContentPresenter → GenerateContentUseCase: execute(modelAlias, prompt)
GenerateContentUseCase → ModelResolutionService: resolveModel(modelAlias)
ModelResolutionService → ModelRepository: findByAlias(alias)
ModelRepository → ModelRepositoryImpl: findByAlias(alias)
ModelRepositoryImpl → GenerateContentUseCase: return Model
ModelResolutionService → GenerateContentUseCase: return Model
GenerateContentUseCase → GoogleVertexAIAdapter: callVertexAI(fullName, prompt, ...)
GoogleVertexAIAdapter → ModelController: return response
ModelController → User: formatted response
```

## Dependency Inversion Principle

```
┌─────────────────────────────────────┐
│             Domain Layer            │
│  ┌─────────────────────────────────┐│
│  │ ModelResolutionService          ││
│  │ (interface)                    ││
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
           ▲ implements
┌─────────────────────────────────────┐
│        Infrastructure Layer         │
│  ┌─────────────────────────────────┐│
│  │ ModelResolutionServiceImpl      ││
│  │ (implementation)                ││
│  │ - modelRepository: ModelRepository││
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
```
