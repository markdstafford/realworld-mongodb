# H2 to MongoDB migration plan

This document outlines a complete strategy for migrating the RealWorld backend application from an H2 database to MongoDB.

### Migration strategy selection

The chosen migration strategy is a phased, profile-based approach designed as a __"lift and shift"__ of the persistence layer. The primary goal is to change the database technology while maintaining as much parity as possible with the original application's relational structure. This is a strategic decision to limit the scope of an already significant undertaking by minimizing changes to the core application logic and adhering to the project's critical constraints.

A __dual-write__ strategy was also considered but ultimately rejected. While it offers benefits for zero-downtime migrations, the added complexity is not warranted for this exercise, especially given the requirement to maintain the application's existing transactional integrity with minimal code changes.

### Task assignment and testing protocol

The detailed task list derived from this plan will be executed by a combination of AI agents and human developers. A primary goal of Phase 0 is to establish a clear testing protocol. If all unit and API tests can be fully automated and executed by an AI agent, they will be. If any test suite requires manual intervention (e.g., a specific setup or validation step), the plan will include explicit __"pause"__ points. At these points, the AI agent will stop and await a signal from a human that the manual tests have been successfully executed before proceeding.

Any new documentation artifacts created during this process (such as testing-protocol.md and changelog.md) should be in Markdown format and stored in a migration folder in the root directory of the repository.

The changelog.md will be updated after every task that modifies the codebase. Each entry should include:

- The reason for the change.
- A code diff showing the before and after of the modified sections.

### Phase 0: Project baseline and health check

__Objective:__ To establish a stable, healthy baseline for the project by verifying its current state, documenting a clear testing protocol, and updating all dependencies.

- __Assess test automation and establish baseline__: Compile the project and run the complete test suite (unit and API tests), documenting the exact steps. The key outcome is to determine if all tests can be reliably automated. If not, the manual steps and validation criteria must be clearly defined. All automatable tests must pass. Any initially failing tests must be fixed before proceeding.  
- __Update dependencies__: Review and update all project dependencies to their latest stable versions. After updating, the build and all tests will be run again (using the protocol defined above) to ensure no breaking changes were introduced. All tests must pass before proceeding.

### Phase 1: Setup and dependency management

__Objective:__ To prepare the development environment for the migration. __The complete test suite will be run against the default H2 configuration after each task to catch regressions immediately.__

- __Integrate MongoDB dependencies__: Add the Spring Boot Starter for MongoDB to the build.gradle.kts file in the persistence module.  
- __Isolate database configurations__: Create separate Spring profiles for h2 and mongodb to manage database-specific beans and properties. Ensure the application continues to use the h2 profile by default.  
- __Configure transaction management__: Add a MongoTransactionManager bean, conditionally enabled only when the mongodb profile is active.

### Phase 2: Data model and persistence layer refactoring

__Objective:__ To iteratively refactor the persistence layer and add corresponding unit tests. __The full test suite (using the H2 profile) must be run after each sub-task to ensure no regressions.__

__Note on module changes:__ Adding @Document annotations to domain models in module/core is a permitted exception to the "changes only in persistence module" rule.

- __Adapt entities for MongoDB__: Add MongoDB-specific annotations (@Document, @Id, etc.) to the existing JPA domain models.  
- __Implement Spring Data MongoDB repositories__: For each entity, create a new repository interface that extends MongoRepository.  
- __Refactor repository adapters__: Update the existing repository adapters to use the new MongoDB repositories. These adapters will need conditional logic to inject either the JPA or the Mongo repository based on the active Spring profile.  
- __Add Unit Tests__: Create unit tests for each new MongoDB adapter, following existing project patterns (e.g., using Mockito to mock the repository).

### Phase 3: Testing and validation

__Objective:__ To ensure the migrated application works correctly with MongoDB.

- __Execute tests against MongoDB__: Run the complete test suite (unit and API tests) with the mongodb profile activated. All tests must pass to confirm the migration's success.  
- __Perform final integration testing__: Once all automated tests are passing, conduct thorough manual testing. This includes running the Postman collection against a server running with the mongodb profile to ensure the API functions as expected in a real-world scenario.

### Migration complete

Once all tasks in Phase 3 are successfully completed, the migration is finished. The h2 profile and old \*JpaRepositoryAdapter.java files can be removed as a final cleanup step.

#### Future considerations

Now that the application is running on MongoDB, further refactoring could be done to better align the data model with idiomatic NoSQL patterns. This was deferred from the initial migration to limit scope, but would include:

- __Embedding documents:__ Instead of using @DBRef and maintaining separate collections for entities like ArticleComment or ArticleFavorite, they could be embedded directly within their parent Article documents. This would improve read performance by reducing the number of queries needed to assemble a complete view.  
- __Optimizing queries:__ The persistence layer could be further optimized to take advantage of MongoDB's powerful query and aggregation capabilities, moving beyond the direct translation of JPA-style queries.