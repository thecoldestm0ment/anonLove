# AnonLove Backend

익명 연애 상담 커뮤니티 AnonLove의 Spring Boot 백엔드 서버입니다.

## 기술 스택

- **Framework**: Spring Boot 4.0.1
- **Language**: Java 17
- **Build Tool**: Gradle 9.2.1
- **Database**: MySQL 8.0
- **Cache**: Redis 7
- **Security**: Spring Security + JWT
- **Real-time**: WebSocket

## 주요 기능

- JWT 기반 인증/인가
- 게시글/댓글 시스템
- 실시간 채팅 (WebSocket)
- 이메일 인증 (Gmail SMTP)
- Redis 세션 관리
- AI 기반 독성 필터링 (연동 예정)

---

## 빠른 시작 (Quick Start)

### 사전 요구사항

- Docker 20.10+
- Docker Compose 2.0+

### 1. 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성합니다:

```env
DB_PASSWORD=anonlove123
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

**환경 변수 설명**:
- `DB_PASSWORD`: MySQL 루트 비밀번호 (기본값: `anonlove123`)
- `MAIL_USERNAME`: Gmail SMTP 계정 (이메일 인증 기능에 필요)
- `MAIL_PASSWORD`: Gmail 앱 비밀번호

> **참고**: Gmail 앱 비밀번호는 [Google 계정 설정](https://myaccount.google.com/apppasswords)에서 생성할 수 있습니다.

### 2. Docker Compose로 실행

```bash
# 전체 스택 시작 (MySQL + Redis + Backend)
docker-compose up -d

# 로그 확인
docker-compose logs -f backend

# 상태 확인
docker-compose ps
```

### 3. 서비스 접속

서비스가 시작되면 다음 주소로 접속할 수 있습니다:

- **Backend API**: `http://localhost:8080`
- **MySQL**: `localhost:3306`
- **Redis**: `localhost:6379`

---

## Docker 이미지 빌드

### 개별 이미지 빌드

```bash
# 이미지 빌드
docker build -t anonlove-backend:latest .

# 이미지 실행
docker run -d \
  --name anonlove-backend \
  -p 8080:8080 \
  -e DB_PASSWORD=your_password \
  -e MAIL_USERNAME=your_email@gmail.com \
  -e MAIL_PASSWORD=your_app_password \
  anonlove-backend:latest
```

### BuildKit 사용 (권장)

```bash
DOCKER_BUILDKIT=1 docker build -t anonlove-backend:latest .
```

---

## 서비스 구성

### Backend

- **포트**: 8080
- **JVM**: OpenJDK JRE 17
- **사용자**: 비루트 사용자 (spring)
- **헬스체크**: 30초 간격, TCP 8080 포트 체크

### MySQL

- **버전**: 8.0
- **포트**: 3306
- **데이터베이스**: anonlove_db
- **타임존**: Asia/Seoul
- **데이터 지속성**: Docker Volume 사용
- **헬스체크**: mysqladmin ping

### Redis

- **버전**: 7-alpine
- **포트**: 6379
- **헬스체크**: redis-cli ping

---

## 네트워크 구성

모든 서비스는 `anonlove-network`라는 bridge 네트워크에서 통신합니다:

| 방향 | 호스트 |
| --- | --- |
| backend → mysql | `mysql:3306` |
| backend → redis | `redis:6379` |
| host → backend | `localhost:8080` |
| host → mysql | `localhost:3306` |
| host → redis | `localhost:6379` |

---

## Docker Compose 명령어

```bash
# 서비스 시작
docker-compose up -d

# 서비스 중지
docker-compose down

# 서비스 중지 + 데이터 삭제
docker-compose down -v

# 로그 실시간 확인
docker-compose logs -f backend

# 특정 서비스 재시작
docker-compose restart backend

# 백그라운드에서 빌드 후 시작
docker-compose up -d --build
```

---

## 헬스체크 및 모니터링

### 컨테이너 상태 확인

```bash
# 전체 컨테이너 상태
docker-compose ps

# 헬스체크 상태 상세
docker inspect anonlove-backend | grep -A 10 Health
docker inspect anonlove-mysql | grep -A 10 Health
docker inspect anonlove-redis | grep -A 10 Health
```

### 로그 확인

```bash
# Backend 로그
docker-compose logs backend

# 실시간 로그 추적
docker-compose logs -f backend

# 최근 100줄
docker-compose logs --tail=100 backend
```

### 연결 테스트

```bash
# MySQL 연결 테스트
docker exec -it anonlove-mysql mysql -u root -panonlove123 anonlove_db

# Redis 연결 테스트
docker exec -it anonlove-redis redis-cli ping
```

---

## 문제 해결

### 컨테이너가 즉시 종료됨

**원인**: 환경 변수 누락 또는 설정 오류

**해결**:
```bash
# 로그 확인
docker-compose logs backend

# 환경 변수 확인
cat .env
```

### DB 연결 실패

**원인**: MySQL이 아직 시작되지 않음

**해결**:
- `depends_on`과 헬스체크로 자동 대기
- 30초 이상 소요될 수 있음
- `docker-compose logs mysql`로 확인

### 메일 발송 실패

**원인**: Gmail 설정 오류

**해결**:
1. Gmail 2단계 인증 활성화
2. 앱 비밀번호 생성
3. `.env` 파일에 정확한 값 입력

### 빌드 실패

**원인**: Gradle Wrapper 실행 권한 문제

**해결**:
```bash
# 로컬에서 권한 부여
chmod +x gradlew

# Docker 데스크톱 메모리 확인 (4GB+ 권장)
```

### 포트 충돌

**원인**: 8080, 3306, 6379 포트가 이미 사용 중

**해결**:
```yaml
# docker-compose.yml에서 포트 변경
ports:
  - "8081:8080"  # 8081로 변경 예시
```

---

## 프로덕션 배포 시 고려사항

### 보안

**⚠️ 데이터베이스 SSL 설정 (중요)**

docker-compose.yml의 `SPRING_DATASOURCE_URL`은 **개발용**으로 SSL이 비활성화되어 있습니다. 프로덕션에서는 반드시 SSL을 활성화하세요:

**개발용 (현재 설정)**:
```yaml
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/anonlove_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
```

**프로덕션용**:
```yaml
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/anonlove_db?useSSL=true&requireSSL=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
```

**SSL 인증서 설정 방법**:

1. **CA 서명 인증서 사용 (권장)**:
```yaml
# MySQL 컨테이너에 인증서 마운트
mysql:
  volumes:
    - ./ssl/ca-cert.pem:/etc/mysql/ssl-ca-cert.pem:ro
    - ./ssl/server-cert.pem:/etc/mysql/server-cert.pem:ro
    - ./ssl/server-key.pem:/etc/mysql/server-key.pem:ro
  environment:
    - MYSQL_SSL_CA=/etc/mysql/ssl-ca-cert.pem
    - MYSQL_SSL_CERT=/etc/mysql/server-cert.pem
    - MYSQL_SSL_KEY=/etc/mysql/server-key.pem

# Backend에 truststore 설정
backend:
  environment:
  - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/anonlove_db?useSSL=true&requireSSL=true&trustCertificateKeyStoreUrl=file:/etc/ssl/mysql.jks&trustCertificateKeyStorePassword=password
  volumes:
    - ./ssl/mysql.jks:/etc/ssl/mysql.jks:ro
```

2. **AWS RDS/Aurora 사용 시**:
```yaml
# AWS RDS는 기본적으로 SSL CA 제공
SPRING_DATASOURCE_URL=jdbc:mysql://your-db.rds.amazonaws.com:3306/anonlove_db?useSSL=true&requireSSL=true
```

**기타 보안 사항**:
- **환경 변수 암호화**: Docker Secrets 또는 외부 시크릿 매니저 사용
- **HTTPS/TLS**: Nginx 역프록시 또는 로드밸런서에서 설정
- **CORS**: 프론트엔드 도메인만 허용하도록 설정
- **DB 비밀번호**: 강력한 비밀번호 사용 (최소 16자, 영문+숫자+특수문자)

### JVM 튜닝

```yaml
environment:
  - JAVA_OPTS=-Xms512m -Xmx1024m
```

### 모니터링

- **로그 수집**: ELK Stack, CloudWatch 등
- **메트릭**: Spring Boot Actuator + Prometheus
- **APM**: Datadog, New Relic 등

---

## 로컬 개발

Docker 없이 로컬에서 실행하려면:

### 사전 요구사항

- Java 17+
- Gradle 9.2.1+
- MySQL 8.0+
- Redis 7+

### 실행 방법

```bash
# 의존성 설치
./gradlew dependencies

# 애플리케이션 실행
./gradlew bootRun
```

---

## 추가 정보

- **AI 서버 연동**: 추후 통합 docker-compose에서 FastAPI 서비스 연동 예정
- **프론트엔드**: 별도 프론트엔드 레포지토리 참고
- **API 문서**: 추후 Swagger/OpenAPI 도입 예정

---

## 라이선스

© 2025 AnonLove. All rights reserved.
