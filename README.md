# 진명에어컨 에이전트 - 에어컨 판매, 설치, AS 통합 관리 시스템

## 1. 프로젝트 소개

진명에어컨 에이전트는 에어컨 판매, 설치, 세척, AS 및 중고 거래까지 모든 과정을 한 곳에서 관리할 수 있는 백엔드 시스템입니다. 사용자는 회원가입 및 소셜 로그인(네이버, 카카오)을 통해 서비스를 이용할 수 있으며, 관리자는 모든 요청을 효율적으로 관리하고 처리할 수 있습니다.

## 2. 주요 기능

- **회원 관리:** 일반 회원가입, 소셜 로그인(네이버, 카카오), 회원 정보 조회/수정, 탈퇴
- **에어컨 설치 신청:** 사용자는 에어컨 설치를 신청하고 견적을 받을 수 있습니다.
- **에어컨 세척 신청:** 사용자는 에어컨 세척 서비스를 신청할 수 있습니다.
- **AS 신청:** 사용자는 에어컨 AS를 신청하고 처리 상태를 확인할 수 있습니다.
- **중고 거래:** 관리자는 중고 에어컨 상품을 등록하고, 사용자는 구매 신청을 할 수 있습니다.
- **공지사항:** 관리자는 사용자에게 중요한 정보를 공지할 수 있습니다.
- **관리자 기능:** 관리자는 모든 신청 내역(설치, 세척, AS)과 회원 목록을 조회하고 관리할 수 있습니다.

## 3. 기술 스택 및 버전

| 구분 | 기술 | 버전 |
| --- | --- | --- |
| **언어** | Java | 21 |
| **프레임워크** | Spring Boot | 3.4.3 |
| **빌드 도구** | Gradle | 8.12.1 |
| **데이터베이스** | MySQL | - |
| **ORM** | Spring Data JPA | - |
| **인증/인가** | Spring Security, JWT | - |
| **API 문서화** | SpringDoc (Swagger UI) | 2.6.0 |
| **라이브러리** | Lombok, Jackson, WebFlux | - |

## 4. API 엔드포인트

API 문서는 서버 실행 후 `http://localhost:8080/swagger-ui.html` 에서 확인하실 수 있습니다.

### 4.1. 인증 (Authentication) - `/api/v1/auth`

| Method | URL | 설명 |
| --- | --- | --- |
| `GET` | `/naver/callback` | 네이버 소셜 로그인 콜백 |
| `GET` | `/kakao/callback` | 카카오 소셜 로그인 콜백 |

### 4.2. 사용자 (User) - `/api/v1/user`

| Method | URL | 설명 |
| --- | --- | --- |
| `POST` | `/join` | 회원가입 |
| `POST` | `/login` | 로그인 |
| `POST` | `/logout` | 로그아웃 |
| `GET` | `/current` | 현재 로그인된 사용자 정보 조회 |
| `GET` | `/all` | (관리자) 모든 사용자 목록 조회 |
| `GET` | `/{userLogin}` | 사용자 상세 정보 조회 |
| `PUT` | `/delete` | 회원 탈퇴 |

### 4.3. 에어컨 설치 (Installation) - `/api/v1/install`

| Method | URL | 설명 |
| --- | --- | --- |
| `POST` | `/post` | 설치 신청 등록 |
| `GET` | `` | 설치 신청 목록 조회 |
| `GET` | `/{installId}` | 설치 신청 상세 조회 |
| `POST` | `/user/{installId}` | (사용자) 비밀번호로 상세 조회 |
| `PUT` | `/{installId}/edit` | (관리자) 설치 신청 수정 |
| `PUT` | `/{installId}/user/edit` | (사용자) 설치 신청 수정 |
| `DELETE`| `/{installId}/delete` | 설치 신청 삭제 |

### 4.4. 에어컨 세척 (Cleaning) - `/api/v1/clean`

| Method | URL | 설명 |
| --- | --- | --- |
| `POST` | `/post` | 세척 신청 등록 |
| `GET` | `` | 세척 신청 목록 조회 |
| `GET` | `/{cleanId}` | 세척 신청 상세 조회 |
| `POST` | `/user/{cleanId}` | (사용자) 비밀번호로 상세 조회 |
| `PUT` | `/user/{cleanId}/edit` | (사용자) 세척 신청 수정 |
| `PUT` | `/admin/{cleanId}/edit` | (관리자) 세척 신청 수정 |
| `DELETE`| `/{cleanId}/delete` | 세척 신청 삭제 |

### 4.5. AS - `/api/v1/service`

| Method | URL | 설명 |
| --- | --- | --- |
| `POST` | `/post` | AS 신청 등록 |
| `GET` | `` | AS 신청 목록 조회 |
| `GET` | `/{asId}` | AS 신청 상세 조회 |
| `POST` | `/user/{asId}` | (사용자) 비밀번호로 상세 조회 |
| `PUT` | `/user/{asId}/edit` | (사용자) AS 신청 수정 |
| `PUT` | `/admin/{asId}/edit` | (관리자) AS 신청 수정 |
| `DELETE`| `/{asId}/delete` | AS 신청 삭제 |

### 4.6. 중고 거래 (Used) - `/api/v1/used`

| Method | URL | 설명 |
| --- | --- | --- |
| `POST` | `/admin/post` | (관리자) 중고 상품 등록 |
| `GET` | `` | 중고 상품 목록 조회 |
| `GET` | `/{usedId}` | 중고 상품 상세 조회 |
| `PUT` | `/{usedId}/edit` | (관리자) 중고 상품 수정 |
| `PUT` | `/{usedId}/sale` | 중고 상품 구매 요청 |
| `DELETE`| `/{usedId}/delete` | (관리자) 중고 상품 삭제 |

### 4.7. 공지사항 (Notice) - `/api/v1/notices`

| Method | URL | 설명 |
| --- | --- | --- |
| `POST` | `/post` | (관리자) 공지사항 등록 |
| `GET` | `` | 공지사항 목록 조회 |
| `GET` | `/{noticeId}` | 공지사항 상세 조회 |
| `PUT` | `/{noticeId}/edit` | (관리자) 공지사항 수정 |
| `DELETE`| `/{noticeId}/delete` | (관리자) 공지사항 삭제 |

## 5. 실행 방법

1. **프로젝트 클론**
   ```bash
   git clone https://github.com/your-username/JMAirBack.git
   cd JMAirBack
   ```

   2. **application.yml 설정**
      `src/main/resources/application.yml` 파일에 데이터베이스 및 소셜 로그인 설정을 추가해야 합니다.
      ```
      spring:
         application:
            name: JMair-agent

      cors:
         allowed-origins: "http://localhost:3000"
         allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
         allowed-headers: "*"

      datasource:
         url:      ${SPRING_DATASOURCE_URL}
         username: ${SPRING_DATASOURCE_USERNAME}
         password: ${SPRING_DATASOURCE_PASSWORD}
         driver-class-name: com.mysql.cj.jdbc.Driver
      
      jpa:
         hibernate:
            ddl-auto: create
         show-sql: true
         database-platform: org.hibernate.dialect.MySQL8Dialect
      
      jwt:
        secret-key: ${JWT_SECRET}  
      
      naver:
         client-id:     ${NAVER_CLIENT_ID}
         client-secret: ${NAVER_CLIENT_SECRET}
         redirect-uri:  "http://localhost:3000"
         uri:           "https://nid.naver.com/oauth2.0/token"
         check-id:      "https://openapi.naver.com/v1/nid/me"
         
      kakao:
         client-id:     ${KAKAO_CLIENT_ID}
         redirect-uri:  "http://localhost:8080/api/v1/auth/kakao/callback"
         token-uri:     "https://kauth.kakao.com/oauth/token"
         profile-uri:   "https://kapi.kakao.com/v2/user/me"
         redirect:      "http://localhost:3000"
      
      server:
         servlet:
            encoding:
               force:   true
               charset: UTF-8
               enabled: true
      ```

   3. **.env 설정**
      `JMAirBack\.env` 파일에 환경변수 값을 추가해야 합니다.
      ```
      MYSQL_DATABASE=
      MYSQL_USER=
      MYSQL_PASSWORD=
      MYSQL_ROOT_PASSWORD=
      
      SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/데이터베이스이름?useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME=
      SPRING_DATASOURCE_PASSWORD=
      
      # JWT Secret
      JWT_SECRET=
      
      # Naver API 
      NAVER_CLIENT_ID=
      NAVER_CLIENT_SECRET=
      
      # Kakao API
      KAKAO_CLIENT_ID=
      ```

3. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

4. **도커컴포즈를 이용한 실행**
   ```bash
   docker-compose build --no-cache --pull
   docker-compose up -d
   ```

5. **API 문서 확인**
   - 브라우저에서 `http://localhost:8080/swagger-ui.html` 로 접속하여 API 문서를 확인합니다.

