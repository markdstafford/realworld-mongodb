# H2 to MongoDB migration task list

This document's primary purpose is to serve as a single source of truth and shared context between the human supervisors and AI agents executing the migration tasks.

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

- [ ] __Task 0.1: Analyze and document the test process.__  
  - [ ] Identify unit test locations in server/api, module/core, and module/persistence.  
  - [ ] Identify the API test runner script in the api-docs directory.  
  - [ ] Document the precise Gradle command to perform a clean build and execute all unit tests (e.g., ./gradlew clean test).  
  - [ ] Document the precise command to execute the API tests, noting any prerequisites.  
  - [ ] Assess and decide if the entire test suite can be run without manual interaction.  
  - [ ] __Success criteria:__ A testing-protocol.md file is created with commands and a clear "Yes/No" on automation feasibility. The test commands should include a clean build step.  
- [ ] __Task 0.2: Execute initial tests and establish baseline.__  
  - [ ] Follow the testing-protocol.md to run all unit tests.  
  - [ ] Follow the testing-protocol.md to run all API tests.  
  - [ ] __Success criteria:__ All tests pass. If not, PAUSE for human intervention to fix them before proceeding.  
- [ ] __Task 0.3: Update project dependencies.__  
  - [ ] Run ./gradlew dependencyUpdates to check for outdated dependencies.  
  - [ ] Modify gradle/libs.versions.toml to update all dependencies to their latest stable versions.  
  - [ ] __Success criteria:__ The gradle/libs.versions.toml file is updated.  
- [ ] __Task 0.4: Validate dependency updates.__  
  - [ ] Execute all unit and API tests according to testing-protocol.md.  
  - [ ] __Success criteria:__ The project builds and all tests pass. If not, PAUSE for human intervention to resolve issues.

### Phase 1: Setup and dependency management

__Objective:__ To prepare the development environment for the migration. __The complete test suite will be run against the default H2 configuration after each task to catch regressions immediately.__

- [ ] __Task 1.1: Integrate MongoDB dependency.__  
  - [ ] Add implementation("org.springframework.boot:spring-boot-starter-data-mongodb") to the dependencies block in module/persistence/build.gradle.kts.  
  - [ ] Update migration/changelog.md with the reason and a diff of the change.  
  - [ ] Run the test suite as defined in testing-protocol.md.  
  - [ ] __Success criteria:__ The dependency is added, the changelog is updated, and all tests pass.  
- [ ] __Task 1.2: Isolate database configurations using profiles.__  
  - [ ] In module/persistence/src/main/resources/, create application-h2.yaml and move the spring.datasource, spring.sql.init, and spring.jpa properties from application.yaml into it.  
  - [ ] In the same directory, create application-mongodb.yaml.  
  - [ ] In server/api/src/main/resources/application.yaml, set spring.profiles.active=h2 to maintain H2 as the default.  
  - [ ] Update migration/changelog.md with the changes.  
  - [ ] Run the test suite.  
  - [ ] __Success criteria:__ Configuration is split into profile-specific files, changelog is updated, and all tests pass.  
- [ ] __Task 1.3: Make configuration beans profile-specific.__  
  - [ ] Add the @Profile("h2") annotation to the JpaConfiguration and DataSourceConfiguration classes in the io.zhc1.realworld.config package.  
  - [ ] Update migration/changelog.md.  
  - [ ] Run the test suite.  
  - [ ] __Success criteria:__ Annotations are added, changelog is updated, and all tests pass.  
- [ ] __Task 1.4: Configure MongoDB transaction management.__  
  - [ ] Create a new class, MongoConfiguration.java, in io.zhc1.realworld.config.  
  - [ ] Annotate the class with @Profile("mongodb") and @Configuration.  
  - [ ] Inside MongoConfiguration, define a bean of type MongoTransactionManager.  
  - [ ] Update migration/changelog.md.  
  - [ ] Run the test suite.  
  - [ ] __Success criteria:__ The MongoConfiguration class is created with the transaction manager bean, the changelog is updated, and all tests pass.

### Phase 2: Data model and persistence layer refactoring

__Objective:__ To iteratively refactor the persistence layer and add corresponding unit tests. __The full test suite (using the H2 profile) must be run after each sub-task to ensure no regressions.__

__Note on module changes:__ Adding @Document annotations to domain models in module/core is a permitted exception to the "changes only in persistence module" rule.

- [ ] __Task 2.1: Refactor User persistence__  
  - [ ] __Annotate User entity__: In User.java, add @Document(collection \= "users"). Update changelog and run tests.  
  - [ ] __Create UserMongoRepository__: In the persistence module, create UserMongoRepository.java extending MongoRepository\<User, UUID\>. Add method signatures equivalent to those in UserJpaRepository. Update changelog and run tests.  
  - [ ] __Isolate JPA Adapter__: Rename UserRepositoryAdapter.java to UserJpaRepositoryAdapter.java. Add @Profile("h2") to the class. Update changelog and run tests.  
  - [ ] __Create Mongo Adapter__: Create UserMongoRepositoryAdapter.java implementing UserRepository. Annotate it with @Component and @Profile("mongodb"). Implement the methods using UserMongoRepository. Update changelog and run tests.  
  - [ ] __Add Unit Tests__: Create UserMongoRepositoryAdapterTest.java in the persistence module's test directory. Write unit tests for the adapter, following existing project patterns (e.g., using Mockito to mock the repository). Update changelog and run tests.  
- [ ] __Task 2.2: Refactor Tag persistence__  
  - [ ] __Annotate Tag entity__: In Tag.java, add @Document(collection \= "tags"). Update changelog and run tests.  
  - [ ] __Create TagMongoRepository__: Create TagMongoRepository.java extending MongoRepository\<Tag, String\>. Update changelog and run tests.  
  - [ ] __Isolate JPA Adapter__: Rename TagRepositoryAdapter.java to TagJpaRepositoryAdapter.java and add @Profile("h2"). Update changelog and run tests.  
  - [ ] __Create Mongo Adapter__: Create TagMongoRepositoryAdapter.java implementing TagRepository. Annotate it with @Component and @Profile("mongodb"). Implement its methods. Update changelog and run tests.  
  - [ ] __Add Unit Tests__: Create TagMongoRepositoryAdapterTest.java and add unit tests for the new adapter, following existing patterns. Update changelog and run tests.  
- [ ] __Task 2.3: Refactor Article persistence__  
  - [ ] __Annotate Article entity__: In Article.java, add @Document(collection \= "articles") and @DBRef to the author and tags fields. Update changelog and run tests.  
  - [ ] __Create ArticleMongoRepository__: Create ArticleMongoRepository.java extending MongoRepository\<Article, UUID\>. Update changelog and run tests.  
  - [ ] __Isolate JPA Adapter__: Rename ArticleRepositoryAdapter.java to ArticleJpaRepositoryAdapter.java and add @Profile("h2"). Update changelog and run tests.  
  - [ ] __Create Mongo Adapter__: Create ArticleMongoRepositoryAdapter.java implementing ArticleRepository. Annotate it with @Component and @Profile("mongodb"). The findAll method will need to be implemented using MongoTemplate and the Criteria API. Update changelog and run tests.  
  - [ ] __Add Unit Tests__: Create ArticleMongoRepositoryAdapterTest.java and add unit tests for the new adapter, following existing patterns. Update changelog and run tests.  
- [ ] __Task 2.4: Refactor remaining entities (ArticleComment, UserFollow, ArticleFavorite)__  
  - [ ] Follow the same pattern for each remaining entity:  
    1. Annotate the entity class.  
    2. Create the MongoRepository interface.  
    3. Rename and profile the existing JPA adapter.  
    4. Create the new MongoDB adapter.  
    5. Create unit tests for the new adapter, following existing patterns.  
    6. Update the changelog and run the full H2 test suite after each step.  
  - [ ] For join-table-like entities (UserFollow, ArticleFavorite), they will be kept as separate collections for minimal impact.

### Phase 3: Testing and validation

__Objective:__ To ensure the migrated application works correctly with MongoDB.

- [ ] __Task 3.1: Execute automated tests against MongoDB.__  
  - [ ] In module/persistence/src/main/resources/application-mongodb.yaml, configure the connection URI to be read from an environment variable (e.g., spring.data.mongodb.uri: ${MONGO\_CONNECTION\_URI}).  
  - [ ] In server/api/src/main/resources/application.yaml, change the active profile from h2 to mongodb.  
  - [ ] Update migration/changelog.md with the configuration changes.  
  - [ ] Run the full test suite as defined in testing-protocol.md, ensuring the MONGO\_CONNECTION\_URI environment variable is set by the operator beforehand.  
  - [ ] __Success criteria:__ All tests pass. If not, PAUSE for human intervention to debug and resolve issues.  
- [ ] __Task 3.2: Perform final integration testing.__  
  - [ ] Ensure the application is running with the mongodb profile active and the MONGO\_CONNECTION\_URI environment variable set.  
  - [ ] Follow the steps in the api-docs/README.md or use the api-docs/Conduit.postman\_collection.json to manually test the API endpoints.  
  - [ ] Verify that all endpoints function as expected and that data is correctly persisted and retrieved from the MongoDB database.  
  - [ ] __Success criteria:__ A human tester confirms that the application is fully functional when connected to MongoDB.

### Migration complete

Once all tasks in Phase 3 are successfully completed, the migration is finished. The h2 profile and old \*JpaRepositoryAdapter.java files can be removed as a final cleanup step.

#### Future considerations

Now that the application is running on MongoDB, further refactoring could be done to better align the data model with idiomatic NoSQL patterns. This was deferred from the initial migration to limit scope, but would include:

- __Embedding documents:__ Instead of using @DBRef and maintaining separate collections for entities like ArticleComment or ArticleFavorite, they could be embedded directly within their parent Article documents. This would improve read performance by reducing the number of queries needed to assemble a complete view.  
- __Optimizing queries:__ The persistence layer could be further optimized to take advantage of MongoDB's powerful query and aggregation capabilities, moving beyond the direct translation of JPA-style queries.