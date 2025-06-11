dependencies {
    implementation(project(":module:core"))

    runtimeOnly(libs.db.h2)

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.spring.boot.starter.p6spy)
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    implementation(libs.cache.caffeine)
}
