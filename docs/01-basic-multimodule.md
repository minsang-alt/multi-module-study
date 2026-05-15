### 멀티모듈이란?

일단, 모듈 이란게 뭘까?
자바에서 모듈이라는건 패키지의 한 단계 위의 집합체이고 
Java에서 모듈이란 패키지의 한 단계 위의 집합체이며, 독립적으로 배포될 수 있는 코드의 단위를 이야기한다. 

멀티 모듈 프로젝트는 상호 연결된 여러개의 모듈로 구성된 프로젝트를 의미한다. 

---

### 시스템(System)과 서비스(Service) 개념

- **서비스(Service)**: 독립적으로 실행 가능한 애플리케이션 하나. `java -jar`로 띄울 수 있는 것.
- **시스템(System)**: 여러 서비스 + 공유 인프라(DB, Redis 등)가 모여 만드는 한 덩어리.
- **멀티모듈 프로젝트**: 하나의 시스템을 표현하는 단위. 1개 이상의 서비스(실행 가능한 앱)를 포함한다.

> 서로 다른 책임을 가진 앱들이 같은 시스템에 속하기 때문에, 하위 모듈에 대한 의존성과 사용성(개방/폐쇄)을 철저히 관리해야 한다.

---

### 프로젝트 구조 한눈에

```
multi-module-study/                 ← 루트 프로젝트 (실행 X, 컨테이너)
├── settings.gradle.kts             ← 어떤 모듈들이 있는지 등록
├── build.gradle.kts                ← 모든 서브모듈 공통 설정
├── gradlew, gradlew.bat            ← Gradle Wrapper (버전 고정)
├── gradle/wrapper/
│
├── module-api/                     ← 웹 앱 (실행 가능)
│   ├── build.gradle.kts
│   └── src/main/java/com/minsang/study/api/
│       ├── ApiApplication.java
│       └── HelloController.java
│
├── module-batch/                   ← 콘솔 앱 (실행 가능)
│   ├── build.gradle.kts
│   └── src/main/java/com/minsang/study/batch/
│       └── BatchApplication.java
│
└── module-common/                  ← 공통 라이브러리 (실행 X)
    ├── build.gradle.kts
    └── src/main/java/com/minsang/study/common/
        └── HelloMessage.java
```

**핵심**: `module-api`, `module-batch`는 **실행 가능한 앱**, `module-common`은 **라이브러리**. 각자 책임이 다르고 의존성도 다르다.

---

### settings.gradle.kts — 모듈 등록

```kotlin
rootProject.name = "multi-module-study"

include("module-api")
include("module-common")
include("module-batch")
```

`include`로 적은 폴더가 서브 프로젝트로 인식된다. 이게 없으면 Gradle은 해당 폴더를 그냥 평범한 디렉토리로 본다.

---

### 루트 build.gradle.kts — 공통 설정

#### plugins 블록 — "버전만 선언, 적용은 안 함"

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.4.0" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}
```

- `java`: Gradle 내장 플러그인. 루트에 적용.
- `apply false`: **버전만 등록**하고, 실제 적용은 서브모듈이 알아서 한다.
  - 왜? 모든 서브모듈에 Spring Boot 플러그인이 필요하지는 않으니까 (`module-common`은 필요 없음).

#### allprojects vs subprojects

```kotlin
allprojects {       // 루트 + 모든 서브 프로젝트에 적용
    group = "com.minsang.study"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {       // 서브 프로젝트에만 적용 (루트 제외)
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
    // ...
}
```

- `allprojects`: 루트도 포함해서 다 적용 (group/version/repositories 같은 메타 정보)
- `subprojects`: 서브에만 적용 (실제 코드 빌드 설정)

#### Java Toolchain

```kotlin
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

- 시스템에 설치된 JDK가 뭐든, **Java 21로 컴파일/실행하라**고 Gradle에 지시.
- Gradle이 알아서 JDK 21을 찾아주거나 다운로드한다.
- 팀원마다 JDK 버전이 달라도 빌드 결과가 일관됨.

#### Spring Boot BOM (Bill Of Materials)

```kotlin
the<DependencyManagementExtension>().apply {
    imports {
        mavenBom(SpringBootPlugin.BOM_COORDINATES)
    }
}
```

**BOM이란**: "서로 호환되는 라이브러리 버전 모음집"

- BOM을 적용하면 의존성 추가할 때 **버전을 안 써도 됨**:
  ```kotlin
  // 버전 X — BOM이 정해진 버전을 자동으로 채워줌
  implementation("org.springframework.boot:spring-boot-starter-web")
  ```
- Spring Boot 3.4.0 BOM은 호환되는 모든 starter, Hibernate, Jackson 등의 버전을 정의함.
- 직접 버전을 박을 일이 거의 없어진다 → 호환성 문제 줄어듦.

---

### plugins vs dependencies — 헷갈리지 말기

| | plugins | dependencies |
|---|---|---|
| 역할 | 빌드 도구의 **기능 확장** | 코드가 쓰는 **라이브러리** |
| 예시 | `java`, `spring-boot`, `kotlin`, `jacoco` | `starter-web`, `lombok`, `mysql-connector` |
| 효과 | `bootJar` 같은 **태스크가 생김** | `import com.xxx` 코드를 쓸 수 있게 됨 |

---

### 모듈별 build.gradle.kts

#### module-common (라이브러리)

```kotlin
// Spring Boot 플러그인을 적용하지 않음 → bootJar 안 만들어짐
// 그냥 일반 JAR로 빌드됨

dependencies {
    // 의도적으로 비움 - 공통 모듈은 가벼워야 한다
}
```

**왜 Spring Boot 플러그인을 적용 안 하나?**
- `module-common`은 실행 가능한 앱이 아니라 라이브러리.
- bootJar(실행 가능 JAR)를 만들 필요 없음.
- 가볍게 유지하기 위해.

#### module-api (웹 앱)

```kotlin
plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":module-common"))                              // 같은 프로젝트 다른 모듈
    implementation("org.springframework.boot:spring-boot-starter-web")     // 웹 서버
}
```

#### module-batch (콘솔 앱)

```kotlin
plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":module-common"))
    implementation("org.springframework.boot:spring-boot-starter")         // 코어만! web 아님!
}
```

**`starter` vs `starter-web` 차이**:
- `starter`: SpringApplication, AutoConfiguration, 로깅 등 코어만 포함. 톰캣 없음.
- `starter-web`: 위에 + 톰캣 + spring-webmvc → 웹 서버가 뜸.

콘솔 앱이니까 `starter`만 써서 톰캣이 안 뜨게 한다.

---

### 의존성 스코프 — implementation vs api vs runtimeOnly vs compileOnly

| 스코프 | 컴파일 시 보임 | 런타임에 있음 | 다른 모듈로 전파 |
|---|---|---|---|
| `implementation` | ✅ | ✅ | ❌ (캡슐화) |
| `api` | ✅ | ✅ | ✅ (전파됨) |
| `runtimeOnly` | ❌ | ✅ | ❌ |
| `compileOnly` | ✅ | ❌ | ❌ |
| `testImplementation` | 테스트만 | 테스트만 | ❌ |

#### implementation vs api 차이 (제일 중요)

```
module-common이 Guava를 implementation으로 추가
  → module-api는 Guava를 직접 못 씀 (숨겨짐)
  → module-common 내부에서만 사용

module-common이 Guava를 api로 추가
  → module-api도 Guava를 자동으로 쓸 수 있음 (전파됨)
  → 의존성이 줄줄이 노출됨
```

**왜 `implementation`이 기본인가**:
1. **캡슐화**: 내부 구현 라이브러리를 외부에 노출하면 나중에 바꾸기 어려움.
2. **빌드 성능**: `api`는 의존하는 모든 모듈의 컴파일 캐시를 무효화 → 빌드 느려짐.

**`api`를 써야 할 때**: 공통 모듈의 공개 API에 그 라이브러리의 타입이 직접 노출될 때만.

---

### 모듈 간 의존성 — project(":module-xxx")

```kotlin
implementation(project(":module-common"))
```

- 외부 라이브러리(예: `"org.springframework:spring-core"`)가 아닌 **같은 프로젝트 안의 다른 모듈**을 의존할 때 쓰는 문법.
- Gradle이 자동으로 의존 모듈을 먼저 빌드해서 클래스패스에 넣어줌.

---

### Gradle Task Graph & Incremental Build

#### 자동 빌드 순서

```bash
./gradlew :module-api:build
```

요청은 module-api만 했지만 Gradle은:
1. 의존성 그래프 분석
2. `module-api`는 `module-common`을 의존함을 인식
3. **자동으로 module-common부터 빌드**

순서:
```
:module-common:compileJava
:module-common:classes
:module-common:jar
:module-api:compileJava       ← common의 JAR을 클래스패스에 넣고 컴파일
:module-api:bootJar
:module-api:build
```

#### UP-TO-DATE 캐시

입력 파일이 안 바뀌면 태스크를 **아예 실행 안 함**. 두 번째 빌드부터 엄청 빨라짐.

```
> Task :module-common:compileJava UP-TO-DATE
> Task :module-api:compileJava UP-TO-DATE
```

#### 무엇이 바뀌면 무엇이 다시 빌드되나

| 바뀐 파일 | module-common 재빌드 | module-api 재빌드 |
|---|---|---|
| `module-common/HelloMessage.java` | ✅ | ✅ (입력 JAR이 바뀜) |
| `module-api/HelloController.java` | ❌ (UP-TO-DATE) | ✅ |

---

### CommandLineRunner — 콘솔 앱의 핵심

```java
@SpringBootApplication
public class BatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner() {
        return args -> {
            // 부팅 완료 후 자동 실행되는 코드
        };
    }
}
```

- `CommandLineRunner`를 빈으로 등록하면 Spring이 **컨텍스트 초기화 직후** `run()`을 호출.
- `run()`이 끝나면 (데몬 스레드 없으면) JVM 종료.
- 웹 앱이 톰캣 때문에 계속 떠있는 것과 대비됨.

**실행 흐름**:
```
SpringApplication.run()
   ↓
컨테이너 초기화, 빈 등록
   ↓
ApplicationContext refresh 완료
   ↓
CommandLineRunner.run() 호출  ← 여기서 배치 로직
   ↓
(콘솔 앱) JVM 종료
```

---

### 의존성 트리 확인하기

```bash
./gradlew :module-batch:dependencies --configuration runtimeClasspath
```

런타임에 클래스패스로 들어가는 모든 라이브러리를 트리로 보여준다.

**관찰 포인트**:
- `module-batch`의 트리에는 tomcat이 없다 → 톰캣 안 뜸
- `module-api`의 트리에는 `tomcat-embed-core`, `spring-webmvc`가 있다 → 웹 서버 뜸

새 의존성 추가할 때마다 이 명령으로 **실제 클래스패스에 뭐가 들어가는지** 확인하는 습관이 중요.

---

### 자주 쓰는 Gradle 명령어

| 명령 | 의미 |
|---|---|
| `./gradlew build` | 모든 모듈 빌드 |
| `./gradlew :module-api:build` | module-api만 빌드 (의존 모듈은 자동 포함) |
| `./gradlew :module-api:bootRun` | module-api 실행 |
| `./gradlew :module-batch:bootRun --args="minsang"` | 인자 전달해서 실행 |
| `./gradlew clean` | 모든 build/ 폴더 삭제 |
| `./gradlew :module-api:dependencies` | 의존성 트리 확인 |
| `./gradlew tasks` | 사용 가능한 모든 태스크 목록 |
| `./gradlew projects` | 프로젝트(모듈) 구조 보기 |

---

### 핵심 마인드셋

> **"의존성은 모듈에 들어가는 게 아니라, 그 모듈의 코드가 끌어쓰는 만큼만 들어간다."**

새 의존성 추가할 때 자문할 것:
1. **누가 쓰나?** — 한 모듈만 쓰면 그 모듈에만. 절대 common에 다 박지 말 것.
2. **코드에서 import 하나?** — Yes → `implementation`, 런타임만 → `runtimeOnly`
3. **다른 모듈에 전파되어야 하나?** — 대부분 아니오 → `implementation`
4. **테스트에서만 쓰나?** — `testImplementation`

---

### 이번 단계 체크리스트

- [ ] settings.gradle.kts와 루트 build.gradle.kts의 역할을 설명할 수 있다
- [ ] `allprojects`와 `subprojects`의 차이를 안다
- [ ] BOM이 뭔지, 왜 쓰는지 설명할 수 있다
- [ ] `plugins` 블록과 `dependencies` 블록의 차이를 안다
- [ ] `implementation`, `api`, `runtimeOnly`, `compileOnly`의 차이를 안다
- [ ] 같은 프로젝트의 다른 모듈을 의존하는 문법을 안다
- [ ] `module-common`에 Spring Boot 플러그인을 안 쓰는 이유를 설명할 수 있다
- [ ] `starter`와 `starter-web`의 차이를 설명할 수 있다
- [ ] CommandLineRunner가 뭔지 안다
- [ ] `dependencies` 태스크로 의존성 트리를 읽을 수 있다

---

### 다음 단계 예고

**2단계**: 안티패턴 직접 체험 — `module-common`에 의존성을 잔뜩 박아서 어떻게 망가지는지 본다.
