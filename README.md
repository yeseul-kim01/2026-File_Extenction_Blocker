# File Extension Blocker (SaaS Backend)

파일 확장자 기반 업로드/전송 제한을 제공하는 SaaS형 파일 보안 정책 관리 백엔드

본 프로젝트는 엔터프라이즈/SaaS 환경에서 요구되는 **정책 관리, 보안 설정, 환경 분리, 운영 가시성**을
Spring Boot 기반으로 구현하는 것을 목표로 한다.

---

## 프로젝트 개요

- 파일 확장자(`.exe`, `.bat`, `.js` 등)에 따라  
  특정 형식의 파일을 **첨부 또는 전송하지 못하도록 제한**
- 관리자가 UI를 통해 **차단 확장자 정책을 설정/변경**
- SaaS 환경을 고려한 **환경 분리(dev/prod), 보안 설정, 로깅 구조** 적용

---

## 아키텍처 개요

```text
Client (Browser)
   ↓
ALB (HTTPS)
   ↓
Spring Boot API (ECS Fargate)
   ↓
RDS (PostgreSQL)
````

### 사용 기술

* **Backend**: Spring Boot (Java)
* **Database**: PostgreSQL (RDS / 로컬 Docker)
* **Infra**: AWS ECS Fargate, ALB
* **Config**: Profile 기반 환경 분리
* **Logging**: Logback + 구조화 로그
* **Security**: Spring Security + CORS 제어

---

## 프로젝트 구조

```text
fileExtenctionBlock
├── src/main/java/dev/project
│   ├── FileExtensionBlockApplication.java
│   ├── common
│   │   └── config
│   │       ├── SecurityConfig.java
│   │       ├── WebConfig.java
│   │       ├── JpaConfig.java
│   │       ├── JacksonConfig.java
│   │       └── AppProperties.java
│   └── web
│       └── api
│           ├── HealthController.java
│           └── VersionController.java
│
├── src/main/resources
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── templates
│       └── policy-extensions.html
│
├── docker-compose.yml
├── .env.dev
└── README.md
```

---

## 환경 설정

### `.env.dev` (개발용 예시)

```env
SPRING_PROFILES_ACTIVE=dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=file_extension
DB_USERNAME=dev_user
DB_PASSWORD=dev_password
GIT_SHA=local-dev
```

> 운영 환경에서는 ECS Task Definition 환경변수로 주입

---

## 실행 방법

### 로컬 DB 실행 (Docker)

```bash
docker compose up -d
```

### 애플리케이션 실행

```bash
./gradlew bootRun
```

---

## 헬스 체크 & 버전 확인

### Health Check

```
GET /health
```

응답:

```json
{ "status": "UP" }
```

### Version 확인

```
GET /version
```

응답:

```json
{
  "version": "1.0.0",
  "gitSha": "8fc403e"
}
```

---

## 보안 및 CORS 정책

* Spring Security 기반 기본 보안 설정
* 허용된 Origin만 API 접근 가능
* 관리 API는 인증/인가 확장 가능 구조로 설계

---

## 운영 관점 고려 사항

* Profile 기반 설정 분리 (`dev` / `prod`)
* CloudWatch 로그 수집을 고려한 로그 포맷
* 헬스 체크 엔드포인트 제공
* Git SHA 기반 버전 추적
