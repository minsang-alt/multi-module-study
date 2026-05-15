plugins {
    id("org.springframework.boot")
}

dependencies {
    // module-common만 의존 - 웹 관련 의존성은 의도적으로 추가하지 않음
    implementation(project(":module-common"))

    // 콘솔 앱이므로 starter-web 대신 가벼운 starter만 사용
    // - SpringApplication, AutoConfiguration, 로깅 등 코어 기능만 포함
    // - 톰캣/스프링MVC는 포함되지 않음 → 웹 서버 안 뜸
    implementation("org.springframework.boot:spring-boot-starter")
}
