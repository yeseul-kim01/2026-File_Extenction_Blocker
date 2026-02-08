ECS에서 CloudWatch Logs
*     - **Log driver: awslogs**
*     - Log group: /ecs/file-extension-block
*     - Stream prefix: ecs**

로컬(dev) 실행:
**SPRING_PROFILES_ACTIVE=dev GIT_SHA=local-test ./gradlew bootRun**
prod 모드 JSON 로그:
**SPRING_PROFILES_ACTIVE=prod GIT_SHA=prod-test ./gradlew bootRun**
