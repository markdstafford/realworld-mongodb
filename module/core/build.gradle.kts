plugins {
    `java-test-fixtures`
}

dependencies {
    implementation(libs.jakarta.persistence.api)
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb") // Added for MongoDB annotations like @Document
    testFixturesImplementation(libs.jakarta.persistence.api)
}
