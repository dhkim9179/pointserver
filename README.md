# Point Server

## Introduction
- 무료포인트를 적립, 사용 및 취소할 수 있는 API 서버

## DB
| 테이블명                        | 설명            |
|-----------------------------|---------------|
| member_point                | 포인트 잔액 관리     |
| member_point_history        | 포인트 이력 관리     |
| member_point_expire         | 포인트 소멸 관리     |
| member_point_usage_detail   | 포인트 소멸의 사용 관리 |
| member_point_cancel         | 포인트 취소 관리     |
| member_point_cancel_history | 포인트 취소 이력 관리  |

## Policy
- 적립, 사용 및 취소 API는 모두 회원아이디(member_id)와 주문번호(order_no)를 기준으로 한다.
- 적립, 사용 및 취소 API는 상세설명 (description)을 반드시 남겨야한다.

## Project Description
### 적립
+ 1회 적립가능 포인트는 1포인트 이상, 10만포인트 이하로 가능하며 1회 최대 적립가능 포인트는 application-${profile}.yml 파일에서 관리한다.
+ 개인별로 보유 가능한 무료포인트의 최대금액 제한이 존재하며 application-${profile}.yml 파일에서 관리한다.
+ 특정 시점에 적립된 포인트는 1원단위까지 어떤 주문에서 사용되었는지 추적할 수 있어야 한다.
  - 확인 가능한 쿼리
```bash

```

+ 포인트 적립은 관리자가 수기로 지급할 수 있으며, 수기지급한 포인트는 다른 적립과 구분되어 식별할 수 있어야 한다.
  - request에 isAdmin을 true로 한 경우, 관리자가 지급한 포인트
  - member_point_expire에 is_admin 컬럼으로 구분할 수 있다.

+ 모든 포인트는 만료일이 존재하며, 최소 1일이상 최대 5년 미만의 만료일을 부여할 수 있다. (기본 365일)
  - request에 expireDay가 포함되면, 최소 1일이상 최대 5년 미만의 만료일인지 확인한다.
  - expireDay가 없으면 기본 365일을 만료일로 설정한다.

### 적립취소
+ 적립할 떄 요청한 회원 아이디와 주문번호로 적립한 금액만큼 취소 가능하며, 적립한 금액중 일부가 사용된 경우라면 적립 취소 될 수 없다.

### 사용
+ 주문시에만 포인트를 사용할 수 있다고 가정한다.
  - 주문만 있다고 가정하고 개발하였다,

+ 포인트 사용시에는 주문번호를 함께 기록하여 어떤 주문에서 얼마의 포인트를 사용했는지 식별할 수 있어야 한다.
  - 확인할 수 있는 쿼리
```bash

```

+ 포인트 사용시에는 관리자가 수기 지급한 포인트가 우선 사용되어야 하며, 만료일이 짧게 남은 순서로 사용해야 한다.

### 사용취소
+ 사용한 금액중 전제 또는 일부를 사용취소 할수 있다.
  - 전체 취소 시 동일한 주문번호를 다시 취소할 수 없다.
  - 부분 취소 시 취소 가능한 금액이 없는 경우 취소할 수 없다.
  - 한번이라도 부분 취소한 경우, 전체 취소할 수 없다.
  - 부분취소로 전체 금액을 취소 요청한 경우, 취소할 수 없다. (전체 취소가 있기 때문)

+ 사용취소 시점에 이미 만료된 포인를 사용취소 해야 한다면 그 금액만큼 신규적립 처리 한다.
  - 신규 적립한 이력을 member_point_history에 남긴다.
  - 해당 이력의 주문번호는 사용주문번호이고, description으로 사용취소 후 신규적립인 것을 확인할 수 있다.
  - 다만, 사용주문번호로 조회하면 사용금액과 신규 적립한 금액이 다를 수 있다.

## Environment
### Prerequisite
- java 21 (JAVA_HOME required)

### Framework
- Spring Boot 3.x
- H2 database
- JPA, Querydsl

## Build & Run
### Build
```bash
./gradlew bootJar
```

## Run App
```bash
java -jar ./build/libs/pointserver-1.0.0-SNAPSHOT.jar --spring.profiles.active=${profileName}
# profileName : 'default' | 'local' | 'dev'
```
