plugins {
    id("org.springframework.boot")
}

dependencies {// Gradle은 이 목록을 보고 자동으로 다운로드하거나 다른 모듈을 빌드해서 클래스패스에 넣어줌.
    implementation(project(":module-common")) // module-common이라는 서브 프로젝트를 가져다 써라 이렇게 하면 module-api에서 module-common의 클래스를 그냥 import 해서 쓸 수 있어요.
    implementation("org.springframework.boot:spring-boot-starter-web")
}

// implementation vs api 스코프:
// module-common이 Guava를 implementation으로 추가 →
//module-api는 Guava를 직접 못 씀 (숨겨짐). module-common이 내부적으로만 사용.
//module-common이 Guava를 api로 추가 →
//module-api도 Guava를 자동으로 쓸 수 있음 (전파됨).

// 왜 implementation이 기본인가
// 캡슐화 때문이에요. 내부 구현 라이브러리를 외부에 노출하면
// 1. 나중에 라이브러리 바꾸기 어려움 (다른 모듈도 다 영향받음)
// 2. 컴파일 캐시 무효화 자주 발생 → 빌드 느려짐