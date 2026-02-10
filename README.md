# File Extension Blocker (SaaS-style Policy API)

## 서비스 개요

테넌트별로 **파일 확장자 차단 정책**을 관리하는 Spring Boot 기반 SaaS 백엔드 서비스입니다.  
고정(fixed) 확장자는 on/off 토글 방식으로,  
커스텀(custom) 확장자는 테넌트별 추가·삭제 방식으로 관리합니다.

본 서비스는 정책을 “설정”하는 것에 그치지 않고,  
실제 파일 업로드·첨부 시점에서 차단 여부를 판단하기 위한  
**정책 소스(Policy Source)** 역할을 수행합니다.

**멀티테넌시 · 운영 · 배포 · 관찰성까지 고려한 SaaS 구조**를 목표로 설계되었습니다.

---

## 실제 사용 시나리오 (How It Works)

### 관리자(테넌트 소유자)

- 관리자 UI 또는 API를 통해:
  - 위험 확장자(`exe`, `bat` 등) 차단 여부 토글
  - 조직 특성에 맞는 커스텀 확장자(`ps1`, `sh` 등) 추가/삭제
- 정책 변경 사항은 **즉시 테넌트 단위로 반영**됩니다.

### 클라이언트(확장 프로그램 / 외부 서비스)

- 최초 1회:
  - **연결 코드 → Tenant 발급** 플로우로 테넌트 식별
- 이후:
  - 모든 정책 API 요청에 `X-Tenant-Id` 헤더 포함
  - 파일 업로드/첨부 전 정책을 조회하여 차단 여부 판단

### 정책 적용 흐름 (End-to-End)

1. 사용자가 파일 업로드 또는 첨부 시도
2. 클라이언트가 파일 확장자 추출
3. 클라이언트 → 정책 API 호출  
   `GET /api/policy/extensions` (Header: `X-Tenant-Id`)
4. 서버는 해당 테넌트의 정책 스냅샷을 반환
5. 클라이언트는 응답 기준으로:
   - 차단 대상 확장자 → 업로드 차단
   - 허용 대상 확장자 → 업로드 진행

> 본 서비스는 **정책 결정을 강제하지 않고**,  
> 정책 데이터를 제공함으로써  
> 다양한 클라이언트가 동일한 정책을 일관되게 적용하도록 설계되었습니다.

---
## Extension Install (Developer Mode)

1. Chrome 주소창에 `chrome://extensions` 접속
2. 우측 상단 **Developer mode** ON
3. `extension.zip` 압축 해제
4. **Load unpacked** 클릭 → 압축 해제한 폴더 선택
5. 확장 프로그램 활성화 확인


## Browser Extension (Client)

본 서비스는 브라우저 확장 프로그램과 함께 사용됩니다.

확장 프로그램은:
- 파일 업로드 시 확장자를 감지하고
- 서버의 정책 API를 조회한 뒤
- 차단 대상 확장자인 경우 업로드를 사전에 차단합니다.

확장 프로그램은 SaaS 서버의 정책을 실제 사용자 환경에 적용하는
Client 역할을 수행합니다.

---

## Live URL

- Web UI: `http://fileext.yeseulkim.cloud`
- Health: `http://fileext.yeseulkim.cloud/health`
- Version: `http://fileext.yeseulkim.cloud/version`

> 현재는 HTTP만 사용

---

## 전체 아키텍처

<img width="1622" height="806" alt="architecture" src="https://github.com/user-attachments/assets/acefa821-c321-42dd-99d6-61a6bb2681b8" />

---

## 핵심 개념

### Tenant 격리
- 모든 정책 API는 `X-Tenant-Id` 헤더로 테넌트를 구분
- 테넌트 간 정책/데이터 완전 분리

### 정책 구조
- **Fixed Extension**
  - 사전 정의된 위험 확장자(exe, bat 등)
  - 차단 여부 토글 방식
- **Custom Extension**
  - 테넌트별로 자유롭게 추가/삭제

### 초기 연결 흐름
- 연결 코드(1회성) → Tenant(API Key 개념) 발급
- 확장 프로그램 또는 외부 클라이언트의 최초 연결 시 사용

---

## API 문서 (Swagger)

- Local Swagger: `http://localhost:8089/docs`
  - ※ 운영(prod) 환경에서는 비활성화
  - 확인을 위해 swagger ui pdf 를 깃허브 상단에 첨부했습니다.
- OpenAPI JSON: `/api-docs`

---

## 인증 / Tenant 헤더

정책 관련 API는 **반드시** 아래 헤더가 필요합니다.

```
X-Tenant-Id
```

---

## 주요 API 흐름

### 1) (관리자 전용) 연결 코드 발급

- `POST /api/connection-codes`
- Header: `X-Tenant-Id`
- 특정 테넌트에 대해 **1회성 연결 코드 발급**

---

### 2) (확장 프로그램) 연결 코드 검증 / 소비

- `POST /api/connection-codes/verify`
- 유효 시 Tenant ID 반환
- **성공 즉시 소비 → 재사용 불가**

---

### 3) (확장 프로그램) Tenant 발급

- `POST /api/tenants/issue`
- SaaS의 API Key 발급 플로우와 유사

---

## Policy Management API

### 고정 확장자 차단 토글

- `PUT /api/policy/extensions/fixed`
- Header: `X-Tenant-Id`
- Body:
  ```json
  {
    "ext": "exe",
    "blocked": true
  }

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

## Tech Stack

### Backend

* Java 17
* Spring Boot 3.x
* Spring Web / Validation / Security(경량)
* Spring Data JPA + Hibernate
* springdoc-openapi (Swagger)

### Database

* RDBMS (MySQL / PostgreSQL 계열)
* `ddl-auto=validate`

### Infrastructure

* AWS ECS (Fargate)
* Application Load Balancer
* Amazon ECR
* Amazon Route53 (ALB Alias)
* CloudWatch Logs

### Build / Container

* Docker / Docker Buildx
* Gradle

---

## 배포 구조 (ECS + ALB + Route53)

```
Client
 └─ Route53 (Alias)
     └─ ALB (HTTP :80)
         └─ Target Group (/health)
             └─ ECS Service (Fargate)
                 └─ Spring Boot (8089)
```

* Health Check Grace Period: **30~60초 권장**
* Rolling Update 기반 무중단 배포

---

## SaaS 설계 포인트

### 멀티테넌시

* `X-Tenant-Id` 기준 모든 데이터 접근 분리

### 보안 경계

* 외부 진입점은 ALB 단일
* `/health` 엔드포인트 인증 제외

### 관찰성

* 구조화 로그
* CloudWatch Logs
* ECS Event + Target Group 상태 기반 트러블슈팅

---

## 로컬 실행

### 환경 변수

* `SPRING_DATASOURCE_URL`
* `SPRING_DATASOURCE_USERNAME`
* `SPRING_DATASOURCE_PASSWORD`
* `SPRING_JPA_HIBERNATE_DDL_AUTO`
* `CORS_ALLOWED_ORIGINS`

### 실행
* 도커 로그인이 완료된 로컬에서 가능함.
* 로컬에서 5433 포트가 사용 가능해야 함.
```bash
docker compose up -d
./gradlew bootRun
# 또는
java -jar build/libs/*.jar
```

* 기본 포트: `8089`
*http://localhost:8089 로 접속 
---

## Troubleshooting

### favicon.ico 500

* 브라우저 자동 요청으로 발생
* 기능 영향 없음
* 필요 시 `static/favicon.ico` 추가 가능

