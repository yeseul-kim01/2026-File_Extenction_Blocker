ECS에서 CloudWatch Logs
*     - **Log driver: awslogs**
*     - Log group: /ecs/file-extension-block
*     - Stream prefix: ecs**

로컬(dev) 실행:
**SPRING_PROFILES_ACTIVE=dev GIT_SHA=local-test ./gradlew bootRun**
prod 모드 JSON 로그:
**SPRING_PROFILES_ACTIVE=prod GIT_SHA=prod-test ./gradlew bootRun**


로그 실행 요구사항
* ECS/Fargate에서 CloudWatch로 바로 수집되는 stdout 로그
* prod에서 JSON 로그 → CloudWatch Insights로 쿼리 가능
* app/env/gitSha 메타 포함 → 배포 버전 추적 가능
* traceId MDC 포함 → 요청 추적 가능
* ACCESS_LOG 별도 로거로 access 로그 분리 가능
* (dev) 메시지 마스킹 컨버터로 안전장치
