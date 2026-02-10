# File Extension Blocker (SaaS-style Policy API)

테넌트별로 **파일 확장자 차단 정책**을 관리하는 Spring Boot 기반 SaaS 백엔드 서비스입니다.  
고정(fixed) 확장자는 on/off 토글, 커스텀(custom) 확장자는 추가/삭제 방식으로 관리합니다.

본 프로젝트는 단순 CRUD API가 아니라,  
**멀티테넌시 · 운영 · 배포 · 관찰성까지 고려한 SaaS 구조**를 목표로 설계되었습니다.

## Live URL

- Web UI: `http://fileext.yeseulkim.cloud`
- Health: `http://fileext.yeseulkim.cloud/health`
- Version: `http://fileext.yeseulkim.cloud/version`

> 현재는 HTTP만 사용  
--
## 아키텍처
<img width="1622" height="806" alt="image" src="https://github.com/user-attachments/assets/acefa821-c321-42dd-99d6-61a6bb2681b8" />

--

## Tech Stack

### Backend (Spring Boot)

* **Java 17**

  * LTS 기반 런타임
  * ECS/Fargate 환경에 적합
* **Spring Boot 3.x**

  * REST API 서버
  * Embedded Tomcat
* **Spring Web (Spring MVC)**

  * 정책 조회/수정 API
  * Health / Version 엔드포인트 제공
* **Spring Validation**

  * 요청 Body 검증 (확장자 형식 등)
* **Spring Security (경량 사용)**

  * 정책 API 보호
  * `/health` 엔드포인트는 인증 제외
  * Tenant 헤더(`X-Tenant-Id`) 기반 접근 제어
* **Spring Data JPA**

  * 테넌트별 정책 데이터 영속화
  * 트랜잭션 관리
* **Hibernate**

  * JPA 구현체
  * 정책/확장자 엔티티 매핑
* **springdoc-openapi (Swagger UI)**

  * OpenAPI 문서 자동 생성
  * `/docs`, `/api-docs` 제공

---

### Database

* **RDBMS (MySQL / PostgreSQL 계열)**

  * 테넌트별 정책 데이터 저장
  * 고정 확장자 / 커스텀 확장자 / 제한 정보 관리
* **JPA 기반 스키마 관리**

  * `ddl-auto=validate` 기본
  * 운영 환경에서 스키마 안정성 중시

---

### Infrastructure / Cloud (AWS)

* **Amazon ECS (Fargate)**

  * 컨테이너 기반 런타임
  * 서버 관리 없는 실행 환경
* **Application Load Balancer (ALB)**

  * 단일 진입점
  * Target Group 기반 트래픽 분산
  * `/health` 기반 헬스체크
* **Amazon ECR**

  * Docker 이미지 저장소
  * `buildx --platform linux/amd64` 기반 멀티 아키텍처 이미지
* **Amazon Route53**

  * 도메인 관리 (`fileext.yeseulkim.cloud`)
  * ALB Alias 레코드 사용
* **Amazon CloudWatch Logs**

  * ECS Task 로그 수집
  * 배포/장애 시 로그 기반 분석

---

### Container / Build

* **Docker**

  * Spring Boot 애플리케이션 컨테이너화
* **Docker Buildx**

  * Mac(M1/M2) 환경에서 `linux/amd64` 이미지 빌드
* **Gradle**

  * 빌드/패키징
  * 실행 JAR 생성

---

### Architecture / Design

* **Multi-Tenant Architecture**

  * `X-Tenant-Id` 헤더 기반 테넌트 분리
  * 모든 정책 로직은 Tenant Scope 내에서 동작
* **SaaS-oriented API Design**

  * 연결 코드 → Tenant 발급 플로우
  * API Key 발급 모델과 유사한 구조
* **Stateless REST API**

  * 세션 미사용
  * 확장 프로그램/웹 클라이언트 모두 대응
* **Rolling Deployment**

  * ECS 무중단 배포
  * Health Check Grace Period 활용

---

### Observability & Operations

* **ALB Health Check**

  * `/health` 엔드포인트 기준
* **ECS Service Event 기반 장애 분석**
* **구조화된 Access Log**

  * method / path / status / duration 중심
* **Git SHA 기반 버전 추적**

  * `/version` 엔드포인트 제공


---

## 핵심 개념

- **Tenant 격리**
  - 모든 정책 API는 `X-Tenant-Id` 헤더로 테넌트를 구분
  - 테넌트 간 정책/데이터 완전 분리
- **정책 구조**
  - Fixed Extension: 사전 정의된 위험 확장자(exe, bat 등)
  - Custom Extension: 테넌트별로 추가/삭제
- **초기 연결 흐름**
  - 연결 코드(1회성) → Tenant(API Key 개념) 발급
  - 확장 프로그램(또는 클라이언트)이 최초 연결 시 테넌트 획득

---

## API 문서 (Swagger)

- Local Swagger: `http://localhost:8089/docs`
> swagger 는 prod 단계에서 비활성화 했기에 접근 불가입니다.
- OpenAPI JSON: `/api-docs`

---

## 인증 / Tenant 헤더

정책 관련 API는 **반드시** 아래 헤더가 필요합니다.

```

X-Tenant-Id

````


---

## 주요 흐름

### 1) (확장 프로그램 - 관리자 UI 전용) 연결 코드 발급

특정 Tenant에 대해 **1회성 연결 코드**를 발급합니다.

- `POST /api/connection-codes`
- Header: `X-Tenant-Id`


---

### 2) (확장 프로그램) 연결 코드 검증/소비

- `POST /api/connection-codes/verify`
- 유효하면 Tenant ID 반환
- **성공 시 즉시 소비 → 재사용 불가**

---

### 3) (확장 프로그램) Tenant(API Key 개념) 발급

확장 프로그램 최초 실행 시 호출합니다.

- `POST /api/tenants/issue`

SaaS에서의 **API Key 발급 플로우와 유사한 개념**입니다.

---

## Policy Management API

### 고정 확장자 차단 토글

사전에 정의된 고정 확장자의 차단 여부를 변경합니다.

- `PUT /api/policy/extensions/fixed`
- Header: `X-Tenant-Id`
- Body 예시:
  ```json
  {
    "ext": "exe",
    "blocked": true
  }
````

* 응답: `204 No Content`
* 오류:

  * `400` 요청 형식 오류
  * `404` 존재하지 않는 고정 확장자

---

### 커스텀 확장자 차단 추가

* `POST /api/policy/extensions/custom`
* Header: `X-Tenant-Id`
* Body:

  ```json
  {
    "ext": "ps1"
  }
  ```
* 응답: `204 No Content`
* 오류:

  * `409` 중복 또는 제한 초과

---

### 커스텀 확장자 차단 삭제

* `DELETE /api/policy/extensions/custom/{ext}`
* Header: `X-Tenant-Id`
* 응답: `204 No Content`
* 오류:

  * `404` 존재하지 않는 확장자

---

### 정책 전체 조회

고정/커스텀 정책과 제한 정보를 하나의 스냅샷으로 반환합니다.

* `GET /api/policy/extensions`
* Header: `X-Tenant-Id`

응답 예시:

```json
{
  "tenantId": "tenant-1",
  "fixed": [
    { "ext": "exe", "blocked": true }
  ],
  "custom": ["ps1"],
  "limit": 200,
  "count": 1
}
```

---

## Health / Version

* `GET /health`

  ```json
  { "status": "ok" }
  ```

* `GET /version`

  ```json
  { "gitSha": "9f3a12c" }
  ```

---

## API 사용 예시 (curl)

### 정책 전체 조회

```bash
curl -X GET http://fileext.yeseulkim.cloud/api/policy/extensions \
  -H "X-Tenant-Id: tenant-1"
```

### 고정 확장자 차단

```bash
curl -X PUT http://fileext.yeseulkim.cloud/api/policy/extensions/fixed \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-1" \
  -d '{"ext":"exe","blocked":true}'
```

### 커스텀 확장자 추가

```bash
curl -X POST http://fileext.yeseulkim.cloud/api/policy/extensions/custom \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-1" \
  -d '{"ext":"ps1"}'
```

### 커스텀 확장자 삭제

```bash
curl -X DELETE http://fileext.yeseulkim.cloud/api/policy/extensions/custom/ps1 \
  -H "X-Tenant-Id: tenant-1"
```

---

## 로컬 실행

### 환경 변수

* `SPRING_DATASOURCE_URL`
* `SPRING_DATASOURCE_USERNAME`
* `SPRING_DATASOURCE_PASSWORD`
* `SPRING_JPA_HIBERNATE_DDL_AUTO` (기본: validate)
* `CORS_ALLOWED_ORIGINS`

### 실행

```bash
./gradlew bootRun
# 또는
java -jar build/libs/*.jar
```

* 기본 포트: `8089`

---

## 배포 구조 (ECS + ALB + Route53)

```
Client
 └─ Route53 (A Record, Alias)
     └─ Application Load Balancer (HTTP:80)
         └─ Target Group (Health: /health)
             └─ ECS Service (Fargate)
                 └─ Spring Boot Container (8089)
```

### 배포 메모

* ALB → TargetGroup → ECS 구조
* TargetGroup Health Check:
  * Path: `/health`
  * Success Code: `200`
* ECS Service:
  * Rolling Update 전략
  * Health Check Grace Period: **30~60초 권장**
* Route53:
  * **ALB Alias 레코드 권장**

---

## SaaS 관점 설계 포인트

### 멀티테넌시

* `X-Tenant-Id` 기준으로 모든 데이터 접근 분리
* 테넌트 간 데이터 공유 없음

### 보안 경계

* 외부 진입점은 ALB 단일
* ECS Task는 ALB SG에서만 접근 허용
* `/health`는 인증 제외

### 관찰성

* 모든 요청은 구조화 로그로 출력
* CloudWatch Logs 연동
* ECS Event + Target Group 상태로 트러블슈팅 가능

### 무중단 배포

* 새 Task Healthy → 기존 Task Draining

---

## Troubleshooting

### favicon.ico 500

* 브라우저 자동 요청으로 발생
* 기능 영향 없음
* 필요 시 `static/favicon.ico` 추가 가능

---

## Roadmap

* 정책 Decision API (`ALLOW / BLOCK`)
* 정책 변경 이력(Audit Log)
* 관리자 UI
* Tenant별 Rate Limit / Quota
* HTTPS(ALB + ACM) 적용

