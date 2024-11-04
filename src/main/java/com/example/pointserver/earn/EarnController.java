package com.example.pointserver.earn;

import com.example.pointserver.common.exception.CustomException;
import com.example.pointserver.common.response.ResponseCode;
import com.example.pointserver.earn.dto.Earn;
import com.example.pointserver.earn.dto.EarnCancel;
import com.example.pointserver.history.model.HistoryInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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
    @PostMapping
    public Earn.Response earn(@Valid @RequestBody Earn.Request request) {
        boolean isNewMember = false;

        // 1회 적립 가능한 최대 금액 확인
        if (request.getPoint() > oneTimeMaxEarnPoint) {
            throw new CustomException(ResponseCode.POINT_EXCEED_LIMIT);
        }

        // 이미 처리된 주문번호인지 확인
        if (earnService.isDuplicateOrderNo(request.getOrderNo())) {
            throw new CustomException(ResponseCode.DUPLICATE_ORDER_NO);
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
            if (request.getExpireDay().isBefore(LocalDate.now().plusDays(minExpirePeriod)) ||
                request.getExpireDay().isAfter(LocalDate.now().plusYears(maxExpirePeriod))
            ) {
                throw new CustomException(ResponseCode.INVALID_EXPIRE_PERIOD);
            }

            // 유효성을 통과한 경우, 요청한 값으로 저장
            expireDay = request.getExpireDay();
        }

        // 포인트 적립
        earnService.earn(
                request.getMemberId(),
                request.getOrderNo(),
                request.getPoint(),
                expireDay,
                request.getDescription(),
                isNewMember,
                request.isAdmin()
        );

        return Earn.Response.builder()
                .orderNo(request.getOrderNo())
                .balance(balance + request.getPoint())
                .build();
    }

    /**
     * 적립 취소
     * @param request EarnCancel.Request
     * @return EarnCancel.Response
     */
    @PostMapping("/cancel")
    public EarnCancel.Response cancel(@Valid @RequestBody EarnCancel.Request request) {
        // 이미 취소한 주문번호인지 확인
        if (earnService.findCancel(request.getMemberId(), request.getOrderNo()) != null) {
            throw new CustomException(ResponseCode.ALREADY_CANCELED);
        }

        // 적립 이력 조회
        HistoryInfo.Earn historyInfo = earnService.findHistory(request.getMemberId(), request.getOrderNo());

        // 이력이 없는 경우
        if (historyInfo == null) {
            throw new CustomException(ResponseCode.NO_HISTORY);
        }

        // 만료일이 지난 경우
        if (historyInfo.getExpireDay().isBefore(LocalDate.now())) {
            throw new CustomException(ResponseCode.EXPIRED);
        }

        // 금액을 사용한 경우
        if (historyInfo.getAmount() != historyInfo.getExpireAmount()) {
            throw new CustomException(ResponseCode.ALREADY_USE_AMOUNT);
        }

        // 적립 취소 이력 생성 및 소멸 금액 차감
        earnService.cancel(
                request.getMemberId(),
                request.getOrderNo(),
                historyInfo.getExpireId(),
                historyInfo.getExpireAmount(),
                request.getDescription()
        );

        return EarnCancel.Response.builder()
                .orderNo(request.getOrderNo())
                .balance(earnService.findBalance(request.getMemberId()))
                .build();
    }
}
