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
- 포인트 적립 및 사용을 거래아이디(transaction_id)와 거래구분(transaction_type)으로 구분할 수 있다.
  - 거래아이디는 주문번호 등 거래를 식별할 수 있는 아이디
    - 거래아이디는 적립 및 사용은 중복해서 사용할 수 없다.
  - 거래아이디가 어떤 거래인지 확인할 수 있는 거래구분(transaction_type)
    - order (주문)
    - admin (관리자)
    - event (이벤트)
    - promotion (프로모션)
- 적립, 사용 및 취소는 상세설명 (description)을 반드시 남겨야한다.

## Requirements
### 적립
+ 1회 최대 적립가능 포인트는 application-${profile}.yml 파일에서 관리한다. (1포인트 이상, 10만포인트 이하)
+ 개인별로 보유 가능한 무료포인트의 최대금액 제한은 application-${profile}.yml 파일에서 관리한다.
+ 특정 시점에 적립된 포인트는 1원단위까지 어떤 주문에서 사용되었는지 추적할 수 있어야 한다.
  - 적립 거래번호로 사용 주문내역을 모두 추적할 수 있는 쿼리
```bash
select
    m2.transaction_id as earnTransactionId, /* 적립 거래번호 */
    m2.expire_amount as expireAmount, /* 소멸금액 */
    m1.amount as usedAmount, /* 소멸금액에서 사용한 금액 */
    (m2.expire_amount + m1.amount) earnAmount, /* 적립 거래번호의 총 적립금액 */
    m3.transaction_id as useTransactionId, /* 사용 주문번호 */
    m3.amount as useAmount /* 사용 금액 */
from member_point_usage_detail m1
join member_point_expire m2 on m1.member_point_expire_id = m2.id
join member_point_history m3 on m1.member_point_history_id = m3.id
where m2.transaction_id = ? /* 적립 거래번호 */
;
```

+ 포인트 적립은 관리자가 수기로 지급할 수 있으며, 수기지급한 포인트는 다른 적립과 구분되어 식별할 수 있어야 한다.
  - request에 transactionType을 admin으로 한 경우, 관리자가 지급한 포인트
  - member_point_expire에 is_admin 컬럼으로 구분할 수 있다.

+ 모든 포인트는 만료일이 존재하며, 최소 1일이상 최대 5년 미만의 만료일을 부여할 수 있다.(기본 365일)
  - request에 expireDay가 포함되면, 최소 1일이상 최대 5년 미만의 만료일인지 확인한다.
  - expireDay가 없으면 기본 365일을 만료일로 설정한다.

### 적립취소
+ 적립할 떄 요청한 회원 아이디와 거래아이디로 적립한 금액만큼 취소 가능하며, 적립한 금액중 일부가 사용된 경우라면 적립 취소 될 수 없다.

### 사용
+ 주문시에만 포인트를 사용할 수 있다고 가정한다.
  - 주문(order) 이외 transactionType은 모두 거절된다.

+ 포인트 사용시에는 주문번호를 함께 기록하여 어떤 주문에서 얼마의 포인트를 사용했는지 식별할 수 있어야 한다.
  - 사용 주문번호로 추적할 수 있는 쿼리
```bash
select
    m2.transaction_id as useTransactionId, /* 사용 주문번호 */
    m2.amount as useAmount, /* 사용금액 */
    m3.transaction_id as earnTransactionId, /* 적립 거래번호 (주문 등) */
    m3.expire_amount, /* 적립 후 소멸될 금액 */
    m1.amount as usedAmount /* 소멸될 금액에서 사용한 금액 */
    /* m3.expire_amount + m1.amount as usedAmount = m3.transaction_id 의 적립 금액 */
from member_point_usage_detail m1
join member_point_history m2 on m1.member_point_history_id = m2.id
join member_point_expire m3 on member_point_expire_id = m3.id
where m2.transaction_id = ? /* 사용 주문번호로 조회 */
; 
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
  - https://www.oracle.com/kr/java/technologies/downloads/

### Framework
- Spring Boot 3.x
- H2 database
- JPA, Querydsl

## Build & Run
### Build
```bash
./gradlew bootJar
```

### Run App
```bash
java -jar ./build/libs/pointserver-1.0.0-SNAPSHOT.jar --spring.profiles.active=${profileName}
# profileName : 'default' | 'local' | 'dev'
```

## API Document
- 서버 실행 후 아래 주소에서 확인할 수 있다.
```bash
http://localhost:8080/swagger-ui/index.html
```

## Test case
### Run Test Case
```bash
./gradlew test
```

### Major Test Cases
| 테스트명           | 클래스명                 | 함수명                   | 설명                                                         | 기대 결과                             |
|----------------|----------------------|-----------------------|------------------------------------------------------------|-----------------------------------|
| 적립             | EarnTest             | earnSuccessTest       | 포인트 적립                                                     | 적립 성공, 잔액 증가                      |
| 적립 취소          | EarnCancelTest       | earnCancelSuccessTest | 포인트 적립 취소                                                  | 취소 성공, 잔액 감소                      |
| 사용 (관리자 적립 없음) | UseTest              | usePointSuccessTestWithoutAdmin          | 1. 만료일이 서로 다른 사용자 적립을 10원씩 2개 적립한다. <br/>2. 5원을 사용한다.      | 사용 성공, 잔액 감소, 만료일이 짧은 소멸금액이 5원 차감 |
| 사용 (관리자 적립 있음) | UseTest              | usePointSuccessTestWithAdmin             | 1. 만료일은 같지만, 관리자 및 사용자 적립을 10원씩 각각 적립한다. <br/>2. 5원을 사용한다. | 사용 성공, 잔액 감소, 관리자 적립 소멸금액이 5원 차감  |
| 사용취소 (전체)      | UseCancelAllTest     | useCancelSuccessTest      | 사용 전체취소 (만료일이 과거가 되도록 수정)                                  | 전체 금액 취소 성공, 잔액 증가, 신규 포인트 적립     |
| 사용취소 (부분)      | UseCancelPartialTest | useCancelSuccessTest      | 사용 부분취소                                                    | 부분 금액 취소 성공, 잔액 증가, 소멸 금액 복원      |
