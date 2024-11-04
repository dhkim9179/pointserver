package com.example.pointserver.use;

import com.example.pointserver.cancel.CancelService;
import com.example.pointserver.common.entity.cancel.MemberPointCancel;
import com.example.pointserver.common.entity.history.MemberPointExpire;
import com.example.pointserver.common.exception.CustomException;
import com.example.pointserver.common.response.ResponseCode;
import com.example.pointserver.expire.ExpireService;
import com.example.pointserver.history.model.HistoryInfo;
import com.example.pointserver.use.detail.UseDetailService;
import com.example.pointserver.use.detail.model.UseDetail;
import com.example.pointserver.use.model.Use;
import com.example.pointserver.use.model.UseCancel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/point/v1/use")
@RequiredArgsConstructor
public class UseController {
    private final UseService useService;
    private final ExpireService expireService;
    private final UseDetailService useDetailService;
    private final CancelService cancelService;

    /**
     * 사용
     * @param request Use.Request
     * @return Use.Response
     */
    @PostMapping
    public Use.Response use(@Valid @RequestBody Use.Request request) {
        // 이미 처리된 주문번호인지 확인
        if (useService.isDuplicateOrderNo(request.getOrderNo())) {
            throw new CustomException(ResponseCode.DUPLICATE_ORDER_NO);
        }

        // 잔액 조회
        Integer balance = useService.findBalance(request.getMemberId());

        // 잔액이 없는 경우 오류
        if (balance == null) {
            throw new CustomException(ResponseCode.NO_BALANCE);
        }

        // 사용 가능한 잔액 확인
        if (balance < request.getPoint()) {
            throw new CustomException(ResponseCode.INSUFFICIENT_BALANCE);
        }

        // 소멸 정보 조회
        List<MemberPointExpire> expires = expireService.findExpires(request.getMemberId());

        // 사용
        useService.use(
                request.getMemberId(),
                request.getOrderNo(),
                request.getPoint(),
                request.getDescription(),
                expires
        );

        return Use.Response.builder()
                .orderNo(request.getOrderNo())
                .balance(balance - request.getPoint())
                .build();
    }

    /**
     * 전체 취소
     * @param request UseCancel.Request
     * @return UseCancel.Response
     */
    @PostMapping("/cancel/all")
    public UseCancel.Response cancelAll(@Valid @RequestBody UseCancel.Request request) {
        // 이미 처리된 취소인지 확인
        if (cancelService.findCancel(request.getMemberId(), request.getOrderNo()) != null) {
            throw new CustomException(ResponseCode.ALREADY_CANCELED);
        }

        // 사용 이력 조회
        HistoryInfo.Use historyInfo = useService.findHistory(request.getMemberId(), request.getOrderNo());

        // 이력이 없는 경우
        if (historyInfo == null) {
            throw new CustomException(ResponseCode.NO_HISTORY);
        }

        // 요청한 금액과 이력 금액이 다른 경우
        if (historyInfo.getAmount() != request.getPoint()) {
            throw new CustomException(ResponseCode.INVALID_CANCEL_AMOUNT);
        }

        // 사용한 이력 조회
        List<UseDetail> useDetailList = useDetailService.findDetail(request.getOrderNo());

        // 취소
        useService.cancelAll(
                request.getMemberId(),
                request.getOrderNo(),
                request.getPoint(),
                request.getDescription(),
                useDetailList
        );

        return UseCancel.Response.builder()
                .balance(useService.findBalance(request.getMemberId()))
                .orderNo(request.getOrderNo())
                .build();
    }

    /**
     * 부분 취소
     * @param request UseCancel.Request
     * @return UseCancel.Response
     */
    @PostMapping("/cancel/partial")
    public UseCancel.Response cancelPartial(@Valid @RequestBody UseCancel.Request request) {
        // 취소 정보 조회
        MemberPointCancel memberPointCancel = cancelService.findCancel(request.getMemberId(), request.getOrderNo());

        // 사용한 이력 조회
        List<UseDetail> useDetailList = useDetailService.findDetail(request.getOrderNo());

        // 부분 취소
        if (memberPointCancel == null) { // 최초 부분 취소
            HistoryInfo.Use historyInfo = useService.findHistory(request.getMemberId(), request.getOrderNo());
            // 전체 취소로 요청
            if (historyInfo.getAmount() == request.getPoint()) {
                throw new CustomException(ResponseCode.REQUIRES_FULL_CANCEL);
            }

            // 취소금액이 잘못됨
            if (historyInfo.getAmount() < request.getPoint()) {
                throw new CustomException(ResponseCode.INVALID_CANCEL_AMOUNT);
            }

            // 취소 후 취소 가능한 금액
            int cancelableAmount = historyInfo.getAmount() - request.getPoint();

            // 부분취소
            useService.cancelPartial(
                    request.getMemberId(),
                    request.getOrderNo(),
                    request.getPoint(),
                    cancelableAmount,
                    request.getDescription(),
                    useDetailList
            );
        } else {
            // 취소할 금액이 없는 경우
            if (memberPointCancel.getCancelableAmount() == 0) {
                throw new CustomException(ResponseCode.NO_CANCEL_AMOUNT);
            }

            // 취소 가능한 금액보다 많은 금액을 요청한 경우
            if (memberPointCancel.getCancelableAmount() < request.getPoint()) {
                throw new CustomException(ResponseCode.INVALID_CANCEL_AMOUNT);
            }

            // 취소 후 취소 가능한 금액
            int cancelableAmount = memberPointCancel.getCancelableAmount() - request.getPoint();

            // 부분취소
            useService.updateCancelPartial(
                    memberPointCancel.getId(),
                    request.getMemberId(),
                    request.getOrderNo(),
                    request.getPoint(),
                    cancelableAmount,
                    request.getDescription(),
                    useDetailList
            );
        }

        return UseCancel.Response.builder()
                .balance(useService.findBalance(request.getMemberId()))
                .orderNo(request.getOrderNo())
                .build();
    }
}
