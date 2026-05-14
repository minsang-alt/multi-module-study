plugins {
    java // Gradle 내장 Java 플러그인을 적용.
    id("org.springframework.boot") version "3.4.0" apply false // 외부 플러그인 버전만 등록, 적용은 서브모듈에서
    id("io.spring.dependency-management") version "1.1.6" apply false
}

allprojects { // "이 안에 쓴 설정을 루트 프로젝트 + 모든 서브 프로젝트에 똑같이 적용해라"
    group = "com.minsang.study" // 프로젝트 식별자의 그룹 ID.
    version = "0.0.1-SNAPSHOT" // SNAPSHOT이 붙으면 개발 중인 버전. 같은 버전이라도 빌드할 때마다 내용이 바뀔 수 있음

    repositories {
        mavenCentral() // 의존성(라이브러리)을 어디서 다운로드할지 지정. mavenCentral()은 가장 표준적인 자바/코틀린 라이브러리 저장소
    }
}

subprojects {
    apply(plugin = "java") // 컴파일/테스트/jar 만드는 능력
    apply(plugin = "io.spring.dependency-management") // 스프링부트 BOM(아래에서 설명)을 쓸 수 있게 해줌

    java {
        toolchain { // Gradle이 알아서 JDK 21을 찾아주는 기능
            languageVersion.set(JavaLanguageVersion.of(21)) // Java 21로 컴파일하고 실행해라.
        }
    }

    // BOM 이란? Bill Of Materials. "자재 명세서"란 뜻인데, 쉽게 말하면 "서로 호환되는 라이브러리 버전 모음집"
    // BOM을 적용하면, 의존성 추가할 때 버전을 안 써도 됨.
    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    dependencies {
        "testImplementation"("org.springframework.boot:spring-boot-starter-test") // 모든 서브 모듈은 테스트할 때 spring-boot-starter-test를 쓴다
    }

    tasks.withType<Test> { // test 타입의 모든 태스크에 이걸 적용해
        useJUnitPlatform() // 테스트는 JUnit 5로 실행
    }
}
