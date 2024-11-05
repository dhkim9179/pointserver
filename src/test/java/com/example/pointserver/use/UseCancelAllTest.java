package com.example.pointserver.use;

import com.example.pointserver.AbstractPointTest;
import com.example.pointserver.common.entity.cancel.MemberPointCancel;
import com.example.pointserver.common.entity.history.MemberPointHistory;
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
import java.time.LocalDate;
import java.util.List;

public class UseCancelAllTest extends AbstractPointTest {

    @Test
    @DisplayName("사용 전체취소 성공")
    public void useCancelSuccessTest() throws Exception {
        int memberId = 423458;
        String transactionId = "order_use_cancel_001";
        String earnTransactionId = transactionId + "1";

        // 요청 셋팅
        EarnRequest earnRequest = EarnRequest.builder()
                .memberId(memberId)
                .transactionId(earnTransactionId)
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

        // 만료일이 과거가 되도록 수정
        expireService.updateExpireDay(memberId, earnTransactionId, LocalDate.now().minusDays(30));

        // 전체 취소 요청
        UseCancelRequest useCancelRequest = UseCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(usePoint)
                .description("취소")
                .build();

        MockHttpServletResponse response = requestUseCancelAll(useCancelRequest);

        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 잔액 확인
        UseCancelResponse useCancelResponse = (UseCancelResponse) JsonUtils.fromJson(response.getContentAsString(), UseCancelResponse.class);
        Assertions.assertThat(useCancelResponse).isNotNull();
        Assertions.assertThat(useCancelResponse.getBalance()).isEqualTo(useResponse.getBalance() + usePoint);

        // 취소 확인
        MemberPointCancel memberPointCancel = cancelService.findCancel(memberId, transactionId);
        Assertions.assertThat(memberPointCancel).isNotNull();
        Assertions.assertThat(memberPointCancel.getMemberId()).isEqualTo(memberId);
        Assertions.assertThat(memberPointCancel.getTransactionId()).isEqualTo(transactionId);
        Assertions.assertThat(memberPointCancel.getAction()).isEqualTo(PointAction.USE.getCode());
        Assertions.assertThat(memberPointCancel.getType()).isEqualTo(CancelType.ALL.getCode());
        Assertions.assertThat(memberPointCancel.getAmount()).isEqualTo(usePoint);
        Assertions.assertThat(memberPointCancel.getCancelableAmount()).isEqualTo(0);

        // 신규 적립 확인
        List<MemberPointHistory> historyList = historyService.findHistory(transactionId);
        Assertions.assertThat(historyList).isNotEmpty();
        Assertions.assertThat(historyList.size()).isEqualTo(2);

        boolean hasNewEarn = false;
        for (MemberPointHistory history : historyList) {
            if (history.getAction().equals(PointAction.EARN.getCode())) {
                hasNewEarn = true;
                break;
            }
        }
        Assertions.assertThat(hasNewEarn).isEqualTo(true);
    }

    @Test
    @DisplayName("사용 전체취소 실패 - 이미 취소내역이 있는 경우")
    public void useCancelFailTest0() throws Exception {
        int memberId = 423451;
        String transactionId = "order_use_cancel_002";

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
        UseCancelRequest useCancelRequest = UseCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(usePoint)
                .description("취소")
                .build();
        requestUseCancelAll(useCancelRequest);
        MockHttpServletResponse response = requestUseCancelAll(useCancelRequest);


        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.ALREADY_CANCELED.getCode());
    }

    @Test
    @DisplayName("사용 전체취소 실패 - 사용 이력이 없는 경우")
    public void useCancelFailTest1() throws Exception {
        int memberId = 423452;
        String transactionId = "order_use_cancel_003";

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
        UseCancelRequest useCancelRequest = UseCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId + "1")
                .point(usePoint)
                .description("취소")
                .build();
        requestUseCancelAll(useCancelRequest);
        MockHttpServletResponse response = requestUseCancelAll(useCancelRequest);


        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.NO_HISTORY.getCode());
    }

    @Test
    @DisplayName("사용 전체취소 실패 - 취소 금액이 다른 경우")
    public void useCancelFailTest2() throws Exception {
        int memberId = 423453;
        String transactionId = "order_use_cancel_004";

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
        UseCancelRequest useCancelRequest = UseCancelRequest.builder()
                .memberId(memberId)
                .transactionId(transactionId)
                .point(usePoint-1)
                .description("취소")
                .build();
        requestUseCancelAll(useCancelRequest);
        MockHttpServletResponse response = requestUseCancelAll(useCancelRequest);


        // 응답 검증
        Assertions.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(response.getContentAsByteArray()).isNotEmpty();

        // 응답 바디 검증
        ErrorResponse errorResponse = (ErrorResponse) JsonUtils.fromJson(new String(response.getContentAsByteArray(), StandardCharsets.UTF_8), ErrorResponse.class);
        Assertions.assertThat(errorResponse.getCode()).isEqualTo(ResponseCode.INVALID_CANCEL_AMOUNT.getCode());
    }
}
