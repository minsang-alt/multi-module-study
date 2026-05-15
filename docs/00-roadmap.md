# 멀티모듈 학습 로드맵

> 사내 멀티모듈 도입 전, 개인 레포에서 실험하며 익히기 위한 학습 계획서.

## 학습 목표

- Gradle 멀티모듈 프로젝트의 구조와 빌드 메커니즘을 손에 익힌다.
- "공통 모듈에 다 박으면 왜 안 되는가"를 머리가 아닌 실험으로 이해한다.
- 책임별 모듈 분리 패턴(domain/storage/web/client 등)을 설계할 수 있다.
- 가상의 시스템을 멀티모듈로 직접 설계하고 구현해본다.
- 사내 코드베이스에 어떻게 적용할지 마이그레이션 전략을 세운다.

---

## 단계별 계획

### 1단계: 멀티모듈 빌드 도구 익히기 ⏳

**기간**: 1~2일
**목표**: Gradle로 모듈을 쪼개서 빌드/실행할 수 있다.

**산출물**:
- [x] `module-api` (웹 앱) + `module-common` (라이브러리) hello world
- [x] `module-batch` 추가 (콘솔 앱, `CommandLineRunner` 사용)
- [x] `dependencies` 태스크로 의존성 트리 읽는 법
- [ ] `docs/01-basic-multimodule.md` 정리

**핵심 키워드**:
- `settings.gradle.kts` `include`
- `subprojects {}`, `allprojects {}`
- `implementation(project(":module-xxx"))`
- `implementation` vs `api` vs `runtimeOnly` vs `compileOnly`
- Spring Boot BOM (`dependency-management` 플러그인)
- Spring Boot 플러그인 적용 vs 비적용 (실행 가능 vs 라이브러리)

---

### 2단계: 안티패턴 직접 체험 🔥

**기간**: 반나절
**목표**: "공통 모듈에 의존성 다 박으면 안 되는 이유"를 실험으로 확인한다.

**실험 시나리오**:

| 실험 | 조작 | 관찰 포인트 |
|---|---|---|
| A | `module-common`에 `starter-web` 추가 | `module-batch` 띄울 때 톰캣이 뜨나? 8080 점유? |
| B | `module-common`에 `starter-data-jpa` 추가 | `module-batch` 부팅 실패 여부, 에러 메시지 |
| C | `api` 스코프로 전파시키기 vs `implementation`으로 막기 | 의존성 노출 차이 |
| D | 부팅 시간 비교 | 의존성이 늘어날수록 부팅이 느려지는지 |

**산출물**:
- [ ] `docs/02-anti-pattern.md` (실험 결과 + 스크린샷 + 로그)

---

### 3단계: 모듈 분리 설계 패턴 학습 📚

**기간**: 2~3일
**목표**: 실무에서 쓰는 모듈 분리 패턴을 안다.

**리서치할 자료**:
- [ ] 우아한형제들 기술블로그 "멀티모듈 설계 이야기 with Gradle, Spring Boot"
- [ ] 토스 SLASH 컨퍼런스 멀티모듈 관련 세션
- [ ] 카카오/라인 기술블로그 멀티모듈 사례
- [ ] jojoldu 블로그 시리즈 (입문~심화)

**배워야 할 설계 패턴**:
- 레이어별 분리: `domain`, `storage`, `web`, `client`, `support`
- 헥사고날/클린 아키텍처와 모듈의 매핑
- 의존성 방향 규칙 (storage → domain, web → domain 등)
- 멀티 어플리케이션 구조 (api / batch / admin / consumer)

**산출물**:
- [ ] `docs/03-design-patterns.md` (패턴별 정리 + 의존성 그래프)
- [ ] `docs/references.md` (참고 자료 링크 모음)

---

### 4단계: 가상 서비스 직접 설계 🛠️

**기간**: 3~5일
**목표**: 책임이 다른 여러 앱이 공통 인프라를 공유하는 시스템을 멀티모듈로 구현한다.

**프로젝트 후보** (하나 선택):
- 미니 쇼핑몰: api-app + admin-app + batch-app + 공통 모듈들
- URL 단축 서비스: api-app + analytics-batch
- 게시판: api + 검색 indexer + admin

**제약 조건**:
- 각 앱이 **서로 다른 의존성 조합**을 갖도록 설계 (안 그러면 멀티모듈 의미 없음)
- 최소 3개 이상의 실행 가능한 앱
- DB 접근 / 외부 API 호출 / 메시징 중 2개 이상 포함

**산출물**:
- [ ] 실제 동작하는 멀티모듈 레포 (지금 이 레포에 디렉토리 분리)
- [ ] `docs/04-design-doc.md` (설계 의도, 모듈 의존성 다이어그램, 트레이드오프)

---

### 5단계: 사내 적용 시뮬레이션 🎯

**기간**: 1~2일
**목표**: 사내 코드베이스를 어떻게 쪼갤지 구체적인 제안서를 만든다.

**할 일**:
- [ ] 현재 사내 프로젝트의 의존성 그래프 그려보기
- [ ] "이 패키지는 어느 모듈로 가야 할까?" 분류 작업
- [ ] 마이그레이션 전략 결정: Big Bang vs Strangler Fig
- [ ] 위험 요소 식별 (테스트 깨짐, 빌드 시간, CI 영향, 팀원 합의 등)

**산출물**:
- [ ] `docs/05-internal-proposal.md` (사내 도입 제안서 초안)

---

## 학습 원칙

1. **읽기 30% / 코딩 70%** — 글만 읽으면 안 남는다. 따라 만들고 부숴봐야 한다.
2. **`docs/`에 정리하면서 진행** — 휘발 방지가 학습의 절반.
3. **"왜?"에 답할 수 있는지 자가 점검** — 예: "왜 domain 모듈은 spring-boot 의존성이 없어야 해?"
4. **각 단계 끝나면 커밋** — 학습 체크포인트 박제.

---

## 진행 상황

- 시작일: 2026-05-13
- 현재 단계: **1단계 마무리 / 2단계 진입 직전**
- 마지막 업데이트: 2026-05-15
