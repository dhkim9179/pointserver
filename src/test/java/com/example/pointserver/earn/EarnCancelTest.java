package com.example.pointserver.earn;

import com.example.pointserver.AbstractPointTest;
import com.example.pointserver.common.entity.cancel.MemberPointCancel;
import com.example.pointserver.common.entity.history.MemberPointExpire;
import com.example.pointserver.common.enums.CancelType;
import com.example.pointserver.common.enums.PointAction;
import com.example.pointserver.common.enums.TransactionType;
import com.example.pointserver.common.response.ErrorResponse;
import com.example.pointserver.common.response.ResponseCode;
import com.example.pointserver.common.utils.JsonUtils;
import com.example.pointserver.earn.model.EarnCancelRequest;
import com.example.pointserver.earn.model.EarnRequest;
import com.example.pointserver.earn.model.EarnResponse;
import com.example.pointserver.use.model.UseRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class EarnCancelTest extends AbstractPointTest {

    @Test
    @DisplayName("적립취소 성공")
    public void earnCancelSuccessTest() throws Exception {
        int memberId = 223456;
        String transactionId = "order_earn_100";
        int point = 10;

        // 적립 요청
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(point)
                .description("적립")
                .build();
        requestEarn(earnRequest);

        // 적립 취소 요청
        EarnCancelRequest earnCancelRequest = EarnCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(point)
                .description("적립취소")
                .build();
        MockHttpServletResponse response = requestEarnCancel(earnCancelRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        EarnResponse earnResponse = (EarnResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), EarnResponse.class);
        Assertions.assertThat(earnResponse.getTransactionId()).isEqualTo(earnRequest.getTransactionId());

        // 취소 확인
        MemberPointCancel memberPointCancel = cancelService.findCancel(memberId, transactionId);
        Assertions.assertThat(memberPointCancel).isNotNull();
        Assertions.assertThat(memberPointCancel.getMemberId()).isEqualTo(memberId);
        Assertions.assertThat(memberPointCancel.getTransactionId()).isEqualTo(transactionId);
        Assertions.assertThat(memberPointCancel.getAction()).isEqualTo(PointAction.EARN.getCode());
        Assertions.assertThat(memberPointCancel.getType()).isEqualTo(CancelType.ALL.getCode());
        Assertions.assertThat(memberPointCancel.getAmount()).isEqualTo(point);
        Assertions.assertThat(memberPointCancel.getCancelableAmount()).isEqualTo(0);

        // 소멸 확인
        List<MemberPointExpire> memberPointExpires = expireService.findExpires(memberId);
        Assertions.assertThat(memberPointExpires).isNotEmpty();
    }

    @Test
    @DisplayName("적립취소 실패 - 거래번호가 다른 경우")
    public void earnCancelFailTest0() throws Exception {
        int memberId = 223457;
        String transactionId = "order_earn_200";
        int point = 10;

        // 적립 요청
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(point)
                .description("적립")
                .build();
        requestEarn(earnRequest);

        // 적립 취소 요청
        EarnCancelRequest earnCancelRequest = EarnCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId + "1")
                .point(point)
                .description("적립취소")
                .build();
        MockHttpServletResponse response = requestEarnCancel(earnCancelRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.NO_HISTORY.getCode());
    }

    @Test
    @DisplayName("적립취소 실패 - 취소금액이 다른 경우")
    public void earnCancelFailTest1() throws Exception {
        int memberId = 223458;
        String transactionId = "order_earn_300";
        int point = 10;

        // 적립 요청
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(point)
                .description("적립")
                .build();
        requestEarn(earnRequest);

        // 취소 요청
        EarnCancelRequest earnCancelRequest = EarnCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(point-1)
                .description("적립취소")
                .build();
        MockHttpServletResponse response = requestEarnCancel(earnCancelRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.INVALID_CANCEL_AMOUNT.getCode());
    }

    @Test
    @DisplayName("적립취소 실패 - 이미 취소가 된 거래번호인 경우")
    public void earnCancelFailTest2() throws Exception {
        int memberId = 223459;
        String transactionId = "order_earn_400";
        int point = 10;

        // 적립 요청
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(point)
                .description("적립")
                .build();
        requestEarn(earnRequest);

        // 취소 요청
        EarnCancelRequest earnCancelRequest = EarnCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(point)
                .description("적립취소")
                .build();
        requestEarnCancel(earnCancelRequest);

        // 동일한 취소 요청
        MockHttpServletResponse response = requestEarnCancel(earnCancelRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.ALREADY_CANCELED.getCode());
    }

    @Test
    @DisplayName("적립취소 실패 - 1원이라도 사용한 경우")
    public void earnCancelFailTest3() throws Exception {
        int memberId = 223460;
        String transactionId = "order_earn_500";
        int point = 10;

        // 적립 요청
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(point)
                .description("적립")
                .build();
        requestEarn(earnRequest);

        Integer balance = earnService.findBalance(memberId);
        if (balance == null) {
            balance = 2;
        }

        // 사용 요청
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId + "1")
                .transactionType(TransactionType.ORDER.getCode())
                .point(balance - 1)
                .description("사용")
                .build();
        requestUse(useRequest);

        // 취소 요청
        EarnCancelRequest earnCancelRequest = EarnCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(point)
                .description("적립취소")
                .build();
        MockHttpServletResponse response = requestEarnCancel(earnCancelRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.ALREADY_USE_AMOUNT.getCode());
    }
}
