package com.example.pointserver.use;

import com.example.pointserver.AbstractPointTest;
import com.example.pointserver.common.entity.history.MemberPointExpire;
import com.example.pointserver.common.entity.history.MemberPointHistory;
import com.example.pointserver.common.enums.PointAction;
import com.example.pointserver.common.enums.TransactionType;
import com.example.pointserver.common.response.ErrorResponse;
import com.example.pointserver.common.response.ResponseCode;
import com.example.pointserver.common.utils.JsonUtils;
import com.example.pointserver.earn.model.EarnRequest;
import com.example.pointserver.use.model.UseRequest;
import com.example.pointserver.use.model.UseResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public class UseTest extends AbstractPointTest {

    @Test
    @DisplayName("사용성공 - 관리자 적립 없음. 만료일이 짧은 순으로 차감")
    public void usePointSuccessTest() throws Exception {
        int memberId = 323458;
        String transactionId = "order_use_001";

        // 적립 요청
        String firstEarnTransactionId = transactionId + "1";
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(firstEarnTransactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(10)
                .description("적립")
                .expireDay(LocalDate.now().plusDays(10))
                .build();

        // 요청
        requestEarn(earnRequest);

        // 작립 요청
        String secondEarnTransactionId = transactionId + "2";
        earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(secondEarnTransactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(10)
                .description("적립")
                .expireDay(LocalDate.now().plusDays(11))
                .build();
        requestEarn(earnRequest);

        int balance = useService.findBalance(memberId);

        // 사용 요청
        int usePoint = 15;
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(usePoint)
                .description("사용")
                .build();
        MockHttpServletResponse response = requestUse(useRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 잔액 확인
        UseResponse useResponse = (UseResponse) JsonUtils.fromJson(response.getContentAsString(), UseResponse.class);
        Assertions.assertThat(useResponse).isNotNull();
        Assertions.assertThat(useResponse.getBalance()).isEqualTo(balance - usePoint);

        // 이력 확인
        List<MemberPointHistory> histories = historyService.findHistory(transactionId);
        Assertions.assertThat(histories).isNotEmpty();
        Assertions.assertThat(histories.getFirst().getMemberId()).isEqualTo(memberId);
        Assertions.assertThat(histories.getFirst().getTransactionId()).isEqualTo(transactionId);
        Assertions.assertThat(histories.getFirst().getTransactionType()).isEqualTo(TransactionType.ORDER.getCode());
        Assertions.assertThat(histories.getFirst().getAction()).isEqualTo(PointAction.USE.getCode());
        Assertions.assertThat(histories.getFirst().getAmount()).isEqualTo(usePoint);

        // 소멸 차감 확인
        List<MemberPointExpire> expires = expireService.findExpires(memberId);
        Assertions.assertThat(expires).isNotEmpty();
        Assertions.assertThat(expires.size()).isEqualTo(2);
        Assertions.assertThat(expires.get(0).getTransactionId()).isEqualTo(firstEarnTransactionId);
        Assertions.assertThat(expires.get(0).getExpireAmount()).isEqualTo(0); // 만료일이 짧은순으로 먼저 차감 15원 중 10원
        Assertions.assertThat(expires.get(1).getTransactionId()).isEqualTo(secondEarnTransactionId);
        Assertions.assertThat(expires.get(1).getExpireAmount()).isEqualTo(5); // 나머지 5원 차감
    }

    @Test
    @DisplayName("사용성공 - 관리자 적립 있음. 만료일은 동일함. 관리자 적립부터 차감해야함")
    public void usePointSuccessTest2() throws Exception {
        int memberId = 333458;
        String transactionId = "order_use_011";

        // 적립 요청
        String firstEarnTransactionId = transactionId + "1";
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(firstEarnTransactionId)
                .transactionType(TransactionType.ADMIN.getCode()) // 관리자 요청
                .point(10)
                .description("적립")
                .expireDay(LocalDate.now().plusDays(10))
                .build();

        // 요청
        requestEarn(earnRequest);

        // 작립 요청
        String secondEarnTransactionId = transactionId + "2";
        earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(secondEarnTransactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(10)
                .description("적립")
                .expireDay(LocalDate.now().plusDays(10))
                .build();
        requestEarn(earnRequest);

        int balance = useService.findBalance(memberId);

        // 사용 요청
        int usePoint = 5;
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(usePoint)
                .description("사용")
                .build();
        MockHttpServletResponse response = requestUse(useRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 잔액 확인
        UseResponse useResponse = (UseResponse) JsonUtils.fromJson(response.getContentAsString(), UseResponse.class);
        Assertions.assertThat(useResponse).isNotNull();
        Assertions.assertThat(useResponse.getBalance()).isEqualTo(balance - usePoint);

        // 이력 확인
        List<MemberPointHistory> histories = historyService.findHistory(transactionId);
        Assertions.assertThat(histories).isNotEmpty();
        Assertions.assertThat(histories.getFirst().getMemberId()).isEqualTo(memberId);
        Assertions.assertThat(histories.getFirst().getTransactionId()).isEqualTo(transactionId);
        Assertions.assertThat(histories.getFirst().getTransactionType()).isEqualTo(TransactionType.ORDER.getCode());
        Assertions.assertThat(histories.getFirst().getAction()).isEqualTo(PointAction.USE.getCode());
        Assertions.assertThat(histories.getFirst().getAmount()).isEqualTo(usePoint);

        // 소멸 차감 확인
        List<MemberPointExpire> expires = expireService.findExpires(memberId);
        Assertions.assertThat(expires).isNotEmpty();
        Assertions.assertThat(expires.size()).isEqualTo(2);
        Assertions.assertThat(expires.get(0).getExpireDay()).isEqualTo(expires.get(1).getExpireDay()); // 만료일은 동일함
        Assertions.assertThat(expires.get(0).getTransactionId()).isEqualTo(firstEarnTransactionId);
        Assertions.assertThat(expires.get(0).getExpireAmount()).isEqualTo(5); // 만료일이 짧은순으로 먼저 차감 15원 중 10원
        Assertions.assertThat(expires.get(0).getAdmin()).isEqualTo(true); // 관리자 적립
        Assertions.assertThat(expires.get(1).getTransactionId()).isEqualTo(secondEarnTransactionId);
        Assertions.assertThat(expires.get(1).getExpireAmount()).isEqualTo(10); // 나머지 5원 차감
        Assertions.assertThat(expires.get(1).getAdmin()).isEqualTo(false); // 관리자 적립 아님
    }

    @Test
    @DisplayName("사용실패 - 주문요청이 아닌 경우")
    public void usePointFailTest0() throws Exception {
        int memberId = 323451;
        String transactionId = "order_use_002";

        // 적립 요청
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId + "1")
                .transactionType(TransactionType.ORDER.getCode())
                .point(10)
                .description("적립")
                .build();
        requestEarn(earnRequest);

        // 사용 요청
        int usePoint = 1;
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.EVENT.getCode())
                .point(usePoint)
                .description("사용")
                .build();
        MockHttpServletResponse response = requestUse(useRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.POINT_USAGE_ORDER_ONLY.getCode());
    }

    @Test
    @DisplayName("사용실패 - 이미 처리한 주문번호인 경우")
    public void usePointFailTest1() throws Exception {
        int memberId = 323452;
        String transactionId = "order_use_003";

        // 요청 셋팅
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId + "1")
                .transactionType(TransactionType.ORDER.getCode())
                .point(10)
                .description("적립")
                .build();
        requestEarn(earnRequest);

        // 사용 요청
        int usePoint = 1;
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(usePoint)
                .description("사용")
                .build();
        // 동일한 거래번호로 2번 요청
        requestUse(useRequest);
        MockHttpServletResponse response = requestUse(useRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.DUPLICATE_TRANSACTION_ID.getCode());
    }

    @Test
    @DisplayName("사용실패 - 잔액 정보가 없는 회원인 경우")
    public void usePointFailTest2() throws Exception {
        int memberId = 323453;
        String transactionId = "order_use_004";

        // 사용 요청
        int usePoint = 1;
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(usePoint)
                .description("사용")
                .build();
        MockHttpServletResponse response = requestUse(useRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.NO_BALANCE.getCode());
    }

    @Test
    @DisplayName("사용실패 - 잔액이 사용금액보다 작은 경우")
    public void usePointFailTest3() throws Exception {
        int memberId = 323455;
        String transactionId = "order_use_005";

        // 적립 요청
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId + "1")
                .transactionType(TransactionType.ORDER.getCode())
                .point(10)
                .description("적립")
                .build();
        requestEarn(earnRequest);

        // 사용 요청
        int usePoint = 20;
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(usePoint)
                .description("사용")
                .build();
        MockHttpServletResponse response = requestUse(useRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.INSUFFICIENT_BALANCE.getCode());
    }
}
