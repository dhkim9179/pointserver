package com.example.pointserver.use;

import com.example.pointserver.AbstractPointTest;
import com.example.pointserver.common.entity.cancel.MemberPointCancel;
import com.example.pointserver.common.enums.CancelType;
import com.example.pointserver.common.enums.PointAction;
import com.example.pointserver.common.enums.TransactionType;
import com.example.pointserver.common.response.ErrorResponse;
import com.example.pointserver.common.response.ResponseCode;
import com.example.pointserver.common.utils.JsonUtils;
import com.example.pointserver.earn.model.EarnRequest;
import com.example.pointserver.use.model.UseCancelRequest;
import com.example.pointserver.use.model.UseCancelResponse;
import com.example.pointserver.use.model.UseRequest;
import com.example.pointserver.use.model.UseResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

public class UseCancelPartialTest extends AbstractPointTest {

    @Test
    @DisplayName("사용 부분취소 성공")
    public void useCancelSuccessTest() throws Exception {
        int memberId = 523458;
        String transactionId = "order_use_cancel_partial_001";

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
        int usePoint = 10;
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(usePoint)
                .description("사용")
                .build();
        MockHttpServletResponse useResponseMock = requestUse(useRequest);
        UseResponse useResponse = (UseResponse) JsonUtils.fromJson(useResponseMock.getContentAsString(), UseResponse.class);

        // 전체 취소 요청
        int cancelPoint = 5;
        int cancelableAmount = usePoint - cancelPoint;
        UseCancelRequest useCancelRequest = UseCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(cancelPoint)
                .description("취소")
                .build();

        MockHttpServletResponse response = requestUseCancelPartial(useCancelRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 잔액 확인
        UseCancelResponse useCancelResponse = (UseCancelResponse) JsonUtils.fromJson(response.getContentAsString(), UseCancelResponse.class);
        Assertions.assertThat(useCancelResponse).isNotNull();
        Assertions.assertThat(useCancelResponse.getBalance()).isEqualTo(useResponse.getBalance() + cancelPoint);

        // 취소 확인
        MemberPointCancel memberPointCancel = cancelService.findCancel(memberId, transactionId);
        Assertions.assertThat(memberPointCancel).isNotNull();
        Assertions.assertThat(memberPointCancel.getMemberId()).isEqualTo(memberId);
        Assertions.assertThat(memberPointCancel.getTransactionId()).isEqualTo(transactionId);
        Assertions.assertThat(memberPointCancel.getAction()).isEqualTo(PointAction.USE.getCode());
        Assertions.assertThat(memberPointCancel.getType()).isEqualTo(CancelType.PARTIAL.getCode());
        Assertions.assertThat(memberPointCancel.getAmount()).isEqualTo(cancelPoint);
        Assertions.assertThat(memberPointCancel.getCancelableAmount()).isEqualTo(cancelableAmount);
    }

    @Test
    @DisplayName("사용 부분취소 실패 - 전체 사용금액을 취소하는 경우")
    public void useCancelFailTest0() throws Exception {
        int memberId = 523457;
        String transactionId = "order_use_cancel_partial_002";

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
        int usePoint = 10;
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(usePoint)
                .description("사용")
                .build();
        requestUse(useRequest);

        // 전체 취소 요청
        int cancelPoint = usePoint;
        UseCancelRequest useCancelRequest = UseCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(cancelPoint)
                .description("취소")
                .build();

        MockHttpServletResponse response = requestUseCancelPartial(useCancelRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.REQUIRES_FULL_CANCEL.getCode());
    }

    @Test
    @DisplayName("사용 부분취소 실패 - 최초 부분 취소 시 금액이 다른 경우")
    public void useCancelFailTest1() throws Exception {
        int memberId = 523456;
        String transactionId = "order_use_cancel_partial_003";

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
        int usePoint = 10;
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(usePoint)
                .description("사용")
                .build();
        requestUse(useRequest);

        // 부분 취소 요청
        int cancelPoint = 20;
        UseCancelRequest useCancelRequest = UseCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(cancelPoint)
                .description("취소")
                .build();

        MockHttpServletResponse response = requestUseCancelPartial(useCancelRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.INVALID_CANCEL_AMOUNT.getCode());
    }

    @Test
    @DisplayName("사용 부분취소 실패 - 최초 부분 취소 시 이력이 없는 경우")
    public void useCancelFailTest2() throws Exception {
        int memberId = 523454;
        String transactionId = "order_use_cancel_partial_004";

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
        int usePoint = 10;
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(usePoint)
                .description("사용")
                .build();
        requestUse(useRequest);

        // 부분 취소 요청
        int cancelPoint = 10;
        UseCancelRequest useCancelRequest = UseCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId + "a")
                .point(cancelPoint)
                .description("취소")
                .build();

        MockHttpServletResponse response = requestUseCancelPartial(useCancelRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.NO_HISTORY.getCode());
    }

    @Test
    @DisplayName("사용 부분취소 실패 - 더 이상 부분취소할 금액이 없는 경우")
    public void useCancelFailTest3() throws Exception {
        int memberId = 523455;
        String transactionId = "order_use_cancel_partial_005";

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
        int usePoint = 10;
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(usePoint)
                .description("사용")
                .build();
        requestUse(useRequest);

        // 부분 취소 요청
        int cancelPoint = 5;
        UseCancelRequest useCancelRequest = UseCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(cancelPoint)
                .description("취소")
                .build();

        // 부분 취소 3번 요청
        requestUseCancelPartial(useCancelRequest);
        requestUseCancelPartial(useCancelRequest);
        MockHttpServletResponse response = requestUseCancelPartial(useCancelRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.NO_CANCEL_AMOUNT.getCode());
    }

    @Test
    @DisplayName("사용 부분취소 실패 - 부분취소 가능한 금액보다 많은 금액을 요청한 경우")
    public void useCancelFailTest4() throws Exception {
        int memberId = 523453;
        String transactionId = "order_use_cancel_partial_006";

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
        int usePoint = 10;
        UseRequest useRequest = UseRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .transactionType(TransactionType.ORDER.getCode())
                .point(usePoint)
                .description("사용")
                .build();
        requestUse(useRequest);

        // 부분 취소 요청
        int cancelPoint = 5;
        UseCancelRequest useCancelRequest = UseCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(cancelPoint)
                .description("취소")
                .build();

        // 부분 취소 요청
        requestUseCancelPartial(useCancelRequest);

        cancelPoint = 6;
        useCancelRequest = UseCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(cancelPoint)
                .description("취소")
                .build();
        MockHttpServletResponse response = requestUseCancelPartial(useCancelRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.INVALID_CANCEL_AMOUNT.getCode());
    }
}
