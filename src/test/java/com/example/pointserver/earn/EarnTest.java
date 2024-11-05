package com.example.pointserver.earn;

import com.example.pointserver.AbstractPointTest;
import com.example.pointserver.common.entity.history.MemberPointExpire;
import com.example.pointserver.common.entity.history.MemberPointHistory;
import com.example.pointserver.common.enums.TransactionType;
import com.example.pointserver.common.response.ErrorResponse;
import com.example.pointserver.common.response.ResponseCode;
import com.example.pointserver.common.utils.JsonUtils;
import com.example.pointserver.earn.model.EarnRequest;
import com.example.pointserver.earn.model.EarnResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public class EarnTest extends AbstractPointTest {

    @Test
    @DisplayName("적립 성공")
    public void earnSuccessTest() throws Exception {
        int memberId = 123456;
        String transactionId = "order_earn_001";

        // 요청 셋팅
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(10)
                .description("적립")
                .build();

        // 요청
        MockHttpServletResponse response = requestEarn(earnRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        EarnResponse earnResponse = (EarnResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), EarnResponse.class);
        Assertions.assertThat(earnResponse.getTransactionId()).isEqualTo(earnRequest.getTransactionId());
        Assertions.assertThat(earnResponse.getBalance()).isEqualTo(earnRequest.getPoint());

        // 이력 확인
        List<MemberPointHistory> histories = historyService.findHistory(transactionId);
        Assertions.assertThat(histories).isNotEmpty();
        Assertions.assertThat(histories.size()).isEqualTo(1);

        // 소멸 확인
        List<MemberPointExpire> memberPointExpires = expireService.findExpires(memberId);
        Assertions.assertThat(memberPointExpires).isNotEmpty();
        Assertions.assertThat(memberPointExpires.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("1회 최대 적립 금액 초과")
    public void pointLimitExceedTest() throws Exception {
        int memberId = 123457;
        String transactionId = "order_earn_002";

        // 요청 셋팅
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(1000000)
                .description("적립")
                .build();

        // 요청
        MockHttpServletResponse response = requestEarn(earnRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.POINT_EXCEED_LIMIT.getCode());
    }

    @Test
    @DisplayName("최대 잔액 초과")
    public void balanceLimitExceedTest() throws Exception {
        int memberId = 123458;
        String transactionId = "order_earn_003";

        int maxBalance = Integer.parseInt(environment.getProperty("policy.max-balance"));
        int maxEarnPoint = Integer.parseInt(environment.getProperty("policy.one-time-max-earn-point"));
        for (int i = 0; i < maxBalance / maxEarnPoint; i++) {
            // 요청 셋팅
            EarnRequest earnRequest = EarnRequest.builder()
                    .memberId(memberId)
                    .transactionId(transactionId + i)
                    .transactionType(TransactionType.ORDER.getCode())
                    .point(maxEarnPoint)
                    .description("적립")
                    .build();

            // 요청
            requestEarn(earnRequest);
        }

        // 요청 셋팅
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(maxEarnPoint)
                .description("적립")
                .build();

        // 요청
        MockHttpServletResponse response = requestEarn(earnRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.BALANCE_EXCEED_LIMIT.getCode());
    }

    @Test
    @DisplayName("만료일 검증 - 1일 미만인 경우")
    public void validExpireDay1() throws Exception {
        int memberId = 123459;
        String transactionId = "order_earn_004";

        // 요청 셋팅
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(10)
                .description("적립")
                .expireDay(LocalDate.now())
                .build();

        // 요청
        MockHttpServletResponse response = requestEarn(earnRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.INVALID_EXPIRE_PERIOD.getCode());
    }

    @Test
    @DisplayName("만료일 검증 - 5년 이상인 경우")
    public void validExpireDay2() throws Exception {
        int memberId = 123460;
        String transactionId = "order_earn_005";
        int expirePeriod = Integer.parseInt(environment.getProperty("policy.expire.max-period"));

        // 요청 셋팅
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(10)
                .description("적립")
                .expireDay(LocalDate.now().plusYears(expirePeriod))
                .build();

        // 요청
        MockHttpServletResponse response = requestEarn(earnRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.INVALID_EXPIRE_PERIOD.getCode());
    }

    @Test
    @DisplayName("만료일 지정이 없는 경우, 만료는 365일 뒤")
    public void validExpireDay3() throws Exception {
        int memberId = 123461;
        String transactionId = "order_earn_006";
        int expirePeriod = Integer.parseInt(environment.getProperty("policy.expire.default-period"));

        // 요청 셋팅
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(10)
                .description("적립")
                .build();

        // 요청
        MockHttpServletResponse response = requestEarn(earnRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        // 만료일 확인
        List<MemberPointExpire> memberPointExpires = expireService.findExpires(memberId);
        Assertions.assertThat(memberPointExpires).isNotEmpty();
        Assertions.assertThat(memberPointExpires.get(0).getExpireDay()).isEqualTo(LocalDate.now().plusDays(expirePeriod));
    }

    @Test
    @DisplayName("관리자 지급")
    public void admin() throws Exception {
        int memberId = 123462;
        String transactionId = "order_earn_007";

        // 요청 셋팅
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ADMIN.getCode())
                .point(10)
                .description("적립")
                .build();

        // 요청
        MockHttpServletResponse response = requestEarn(earnRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        // 괸리자 확인
        List<MemberPointExpire> memberPointExpires = expireService.findExpires(memberId);
        Assertions.assertThat(memberPointExpires).isNotEmpty();
        Assertions.assertThat(memberPointExpires.get(0).getAdmin()).isEqualTo(true);
    }
}
