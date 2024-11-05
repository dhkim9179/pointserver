package com.example.pointserver.earn;

import com.example.pointserver.common.exception.CustomException;
import com.example.pointserver.common.response.ErrorResponse;
import com.example.pointserver.common.response.ResponseCode;
import com.example.pointserver.earn.model.EarnCancelRequest;
import com.example.pointserver.earn.model.EarnCancelResponse;
import com.example.pointserver.earn.model.EarnRequest;
import com.example.pointserver.earn.model.EarnResponse;
import com.example.pointserver.history.model.HistoryInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "적립 컨트롤러")
@RestController
@RequestMapping("/point/v1/earn")
@RequiredArgsConstructor
public class EarnController {

    @Value("${policy.max-balance}")
    private int maxBalance;

    @Value("${policy.one-time-max-earn-point}")
    private int oneTimeMaxEarnPoint;

    @Value("${policy.expire.default-period}")
    private int defaultExpirePeriod;

    @Value("${policy.expire.min-period}")
    private int minExpirePeriod;

    @Value("${policy.expire.max-period}")
    private int maxExpirePeriod;

    private final EarnService earnService;

    /**
     * 적립
     * @param request Earn.Request
     * @return Earn.Response
     */
    @Operation(summary = "적립", description = "포인트 적립")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "적립 성공", content = @Content(schema = @Schema(implementation = EarnResponse.class))),
                    @ApiResponse(responseCode = "400", description = "1회 최대 적립 금액 초과, 최대 잔액 초과, 거래번호 중복, 만료일 유효성 확인 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            }
    )
    @PostMapping
    public EarnResponse earn(@Valid @RequestBody EarnRequest request) {
        boolean isNewMember = false;

        // 1회 적립 가능한 최대 금액 확인
        if (request.getPoint() > oneTimeMaxEarnPoint) {
            throw new CustomException(ResponseCode.POINT_EXCEED_LIMIT);
        }

        // 이미 처리된 주문번호인지 확인
        if (earnService.isDuplicateTransactionId(request.getTransactionId())) {
            throw new CustomException(ResponseCode.DUPLICATE_TRANSACTION_ID);
        }

        // 포인트 잔액 조회
        Integer balance = earnService.findBalance(request.getMemberId());

        // 신규 회원인 경우
        if (balance == null) {
            balance = 0;
            isNewMember = true;
        }

        // 최대 잔액 확인
        if (balance + request.getPoint() > maxBalance) {
            throw new CustomException(ResponseCode.BALANCE_EXCEED_LIMIT);
        }

        // 만료일 확인
        LocalDate expireDay = LocalDate.now().plusDays(defaultExpirePeriod);
        if (request.getExpireDay() != null) {
            // 최소 1일 ~ 최대 5년
            if (request.getExpireDay().isBefore(LocalDate.now().plusDays(minExpirePeriod)) ||  // 1일 이상
                !(request.getExpireDay().isBefore(LocalDate.now().plusYears(maxExpirePeriod))) // 5년 미만
            ) {
                throw new CustomException(ResponseCode.INVALID_EXPIRE_PERIOD);
            }

            // 유효성을 통과한 경우, 요청한 값으로 저장
            expireDay = request.getExpireDay();
        }

        // 포인트 적립
        earnService.earn(
                request.getMemberId(),
                request.getTransactionId(),
                request.getTransactionType(),
                request.getPoint(),
                expireDay,
                request.getDescription(),
                isNewMember
        );

        return EarnResponse.builder()
                .transactionId(request.getTransactionId())
                .balance(balance + request.getPoint())
                .build();
    }

    /**
     * 적립 취소
     * @param request EarnCancel.Request
     * @return EarnCancel.Response
     */
    @Operation(summary = "적립취소", description = "포인트 적립취소")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "적립취소 성공", content = @Content(schema = @Schema(implementation = EarnCancelResponse.class))),
                    @ApiResponse(responseCode = "400", description = "이미 취소한 경우, 적립 이력이 없는 경우, 이미 만료된 경우, 취소요청금액이 잘못된 경우, 1원이라도 사용한 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            }
    )
    @PostMapping("/cancel")
    public EarnCancelResponse cancel(@Valid @RequestBody EarnCancelRequest request) {
        // 이미 취소한 주문번호인지 확인
        if (earnService.findCancel(request.getMemberId(), request.getTransactionId()) != null) {
            throw new CustomException(ResponseCode.ALREADY_CANCELED);
        }

        // 적립 이력 조회
        HistoryInfo.Earn historyInfo = earnService.findHistory(request.getMemberId(), request.getTransactionId());

        // 이력이 없는 경우
        if (historyInfo == null) {
            throw new CustomException(ResponseCode.NO_HISTORY);
        }

        // 만료일이 지난 경우
        if (historyInfo.getExpireDay().isBefore(LocalDate.now())) {
            throw new CustomException(ResponseCode.EXPIRED);
        }

        // 요청한 금액과 이력 금액이 다른 경우
        if (request.getPoint() != historyInfo.getAmount()) {
            throw new CustomException(ResponseCode.INVALID_CANCEL_AMOUNT);
        }

        // 금액을 사용한 경우
        if (historyInfo.getAmount() != historyInfo.getExpireAmount()) {
            throw new CustomException(ResponseCode.ALREADY_USE_AMOUNT);
        }

        // 적립 취소 이력 생성 및 소멸 금액 차감
        earnService.cancel(
                request.getMemberId(),
                request.getTransactionId(),
                historyInfo.getExpireId(),
                historyInfo.getExpireAmount(),
                request.getDescription()
        );

        return EarnCancelResponse.builder()
                .transactionId(request.getTransactionId())
                .balance(earnService.findBalance(request.getMemberId()))
                .build();
    }
}
