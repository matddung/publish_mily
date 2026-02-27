MILY
====

----

## 프로젝트 개발 동기
법에 대해 잘 모를 때, 법이 필요한 경우에 사용할 수 있는 페이지를 만들어보았습니다.

---

내가 맡은 역할
-------------
- 일반 멤버 테이블과 변호사 멤버 테이블을 분리하여 정보 저장
- 견적서 엔티티, API 구현
- 예약 엔티티, API 구현
- 변호사 멤버 가입시 이미지 기입 구현

---

## ⚡ Quick Start

1. 서버 실행

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

2실행 확인
- 기본 주소: `http://localhost:8080`

---

### ✍ Achieved
- 예약 생성 로직을 단일 서비스 메서드로 통합하고, 트랜잭션 범위 내에서 처리하도록 구조 개선
- DB 레벨 Unique Constraint 추가를 통해 Race Condition 상황에서도 최종 정합성 보장
- 애플리케이션 레벨 검증 + DB 레벨 제약을 병행 적용한 방어적 설계 적용
- 중복 예약 시나리오 테스트 케이스 추가 및 검증

---

![mily main](https://github.com/user-attachments/assets/ae540be2-7d2a-4308-a1c3-54e0daad51ca)
---------------
origin repository adress : https://github.com/final-project-gang/mily
-

Demonstration video adress : https://www.youtube.com/watch?v=G7RHVrFfPSo&t=376s
-

----------------

#### 일반 유저는
- 모르는 것에 대한 질문을 올려서 변호사들에게 답변을 받을 수 있습니다.
- 자신이 원하는 시간에 변호사와의 상담을 예약할 수 있습니다.
- 자신이 처한 상황에 대해서 견적서를 변호사들에게 보낼 수 있습니다.
- 변호사의 이력과 전문성에 대해 손쉽게 파악할 수 있습니다.

#### 변호사 유저는
- 일반 유저가 올린 질문에 답변을 달아서 포인트를 얻을 수 있습니다.
- 자신에게 도착한 견적서로 유저에게 연락할 수 있습니다.
- 자신을 좀 더 쉽고 편하게 소개할 수 있습니다.

---

프로젝트 소개
------------
회원가입, 로그인, 게시판, 댓글, 예약, 견적서, 결제의 기능을 구현하였습니다.

로그인 시, 멤버의 권한에 따라 사용할 수 있는 기능의 차이를 두었습니다.

멤버 테이블을 하나로 두고, 멤버 테이블을 참조하는 변호사 멤버 테이블을 추가하여, 변호사 유저의 정보만 따로 담는 테이블을 하나 더 구성하였습니다.

스프링 시큐리티를 통해서 비밀번호를 암호화하고, 세션을 통한 인가로 로그인이 진행되었습니다.

게시판을 통해 변호사에게 간단한 상담글을 작성할 수 있고, 변호사는 댓글 채택을 통해 포인트를 얻을 수 있습니다.

견적서를 보내 여러 명의 변호사들에게 자신의 상황을 알리고 예약을 진행할 수 있습니다.

토스 페이먼츠를 구현해 테스트 결제를 구현해 보았습니다.

---

개발 기간
--------
2023.10.10 ~ 2023.12.06

멤버 구성
---------
- nowJun82(이재준, 팀장)
- jogyoulsam(조겨울샘, 조원)
- matddung(윤준혁, 조원)
- rudqja1127(박경범, 조원)

---

개발 환경
--------
- Spring Boot 3.1.4, Java 17
- Spring Security
- Spring Data JPA
- Maria DB
- Gradle 7.6.1
- tika
- Lombok

---

ERD
---
<a href="https://ibb.co/Z1gqytM"><img src="https://i.ibb.co/PzWqB36/MILY-1.png" alt="MILY-1" border="0"></a>

패키지 구조
----------
```
milyReadMe
└─📦src
   ├─📂main
   │  ├─📂java
   │  │  └─📂com
   │  │     └─📂mily
   │  │        ├─ 📂article // 게시판
   │  │        ├─ 📂base // InitializeData, RsData
   │  │        ├─ 📂Email // email 임시 비밀번호 전송
   │  │        ├─ 📂estimate // 견적서
   │  │        ├─ 📂image // 변호사 프로필 이미지 서비스
   │  │        ├─ 📜MilyApplication.java
   │  │        ├─ 📜MilyHomeController.java // 메인 페이지 컨트롤러
   │  │        ├─ 📂payment // 토스 페이먼츠
   │  │        ├─ 📂reservation // 예약
   │  │        ├─ 📂security // Spring Security
   │  │        ├─ 📂standard // Util 페이지(여러가지 편의 도구함)
   │  │        │  └─ 📂util
   │  │        └─ 📂user // 일반 유저와 변호사 유저 테이블
   │  └─ 📂resources
   │     ├─ 📜application.yml // 프로젝트 설정
   │     ├─ 📂static
   │     │  └─ 📂resource
   │     │     ├─ 📂common
   │     │     └─ 📂server
   │     └─ 📂templates
   └─ 📂test
```
