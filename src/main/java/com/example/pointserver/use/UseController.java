package com.example.pointserver.use;

import com.example.pointserver.cancel.CancelService;
import com.example.pointserver.common.entity.cancel.MemberPointCancel;
import com.example.pointserver.common.entity.history.MemberPointExpire;
import com.example.pointserver.common.enums.TransactionType;
import com.example.pointserver.common.exception.CustomException;
import com.example.pointserver.common.response.ErrorResponse;
import com.example.pointserver.common.response.ResponseCode;
import com.example.pointserver.expire.ExpireService;
import com.example.pointserver.history.model.HistoryInfo;
import com.example.pointserver.use.detail.UseDetailService;
import com.example.pointserver.use.detail.model.UseDetail;
import com.example.pointserver.use.model.UseCancelRequest;
import com.example.pointserver.use.model.UseCancelResponse;
import com.example.pointserver.use.model.UseRequest;
import com.example.pointserver.use.model.UseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "사용 컨트롤러")
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
    @Operation(summary = "사용", description = "포인트 사용")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "사용 성공", content = @Content(schema = @Schema(implementation = UseResponse.class))),
                    @ApiResponse(responseCode = "400", description = "거래구분이 주문인 경우만 사용가능, 이미 처리된 주문번호, 잔액정보가 없는 경우, 잔액보다 사용금액이 큰 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            }
    )
    @PostMapping
    public UseResponse use(@Valid @RequestBody UseRequest request) {
        // 주문만 사용가능
        if (!request.getTransactionType().equals(TransactionType.ORDER.getCode())) {
            throw new CustomException(ResponseCode.POINT_USAGE_ORDER_ONLY);
        }

        // 이미 처리된 주문번호인지 확인
        if (useService.isDuplicateTransactionId(request.getTransactionId())) {
            throw new CustomException(ResponseCode.DUPLICATE_TRANSACTION_ID);
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
                request.getTransactionId(),
                request.getTransactionType(),
                request.getPoint(),
                request.getDescription(),
                expires
        );

        return UseResponse.builder()
                .transactionId(request.getTransactionId())
                .balance(balance - request.getPoint())
                .build();
    }

    /**
     * 전체 취소
     * @param request UseCancel.Request
     * @return UseCancel.Response
     */
    @Operation(summary = "사용 전체취소", description = "포인트 사용 전체취소")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "전체취소 성공", content = @Content(schema = @Schema(implementation = UseCancelResponse.class))),
                    @ApiResponse(responseCode = "400", description = "이미 취소한 경우, 사용이력이 없는 경우, 전액 취소를 요청한 경우, 취소요청금액이 잘못된 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            }
    )
    @PostMapping("/cancel/all")
    public UseCancelResponse cancelAll(@Valid @RequestBody UseCancelRequest request) {
        // 이미 처리된 취소인지 확인
        if (cancelService.findCancel(request.getMemberId(), request.getTransactionId()) != null) {
            throw new CustomException(ResponseCode.ALREADY_CANCELED);
        }

        // 사용 이력 조회
        HistoryInfo.Use historyInfo = useService.findHistory(request.getMemberId(), request.getTransactionId());

        // 이력이 없는 경우
        if (historyInfo == null) {
            throw new CustomException(ResponseCode.NO_HISTORY);
        }

        // 요청한 금액과 이력 금액이 다른 경우
        if (historyInfo.getAmount() != request.getPoint()) {
            throw new CustomException(ResponseCode.INVALID_CANCEL_AMOUNT);
        }

        // 사용한 이력 조회
        List<UseDetail> useDetailList = useDetailService.findDetail(request.getTransactionId());

        // 취소
        useService.cancelAll(
                request.getMemberId(),
                request.getTransactionId(),
                request.getPoint(),
                request.getDescription(),
                useDetailList
        );

        return UseCancelResponse.builder()
                .balance(useService.findBalance(request.getMemberId()))
                .transactionId(request.getTransactionId())
                .build();
    }

    /**
     * 부분 취소
     * @param request UseCancel.Request
     * @return UseCancel.Response
     */
    @Operation(summary = "사용 부분취소", description = "포인트 사용 부분취소")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "부분취소 성공", content = @Content(schema = @Schema(implementation = UseCancelResponse.class))),
                    @ApiResponse(responseCode = "400", description = "전액 취소를 요청한 경우, 취소요청금액이 잘못된 경우, 더이상 취소할 금액이 없는 경우", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            }
    )
    @PostMapping("/cancel/partial")
    public UseCancelResponse cancelPartial(@Valid @RequestBody UseCancelRequest request) {
        // 취소 정보 조회
        MemberPointCancel memberPointCancel = cancelService.findCancel(request.getMemberId(), request.getTransactionId());

        // 사용한 이력 조회
        List<UseDetail> useDetailList = useDetailService.findDetail(request.getTransactionId());

        // 부분 취소
        if (memberPointCancel == null) { // 최초 부분 취소
            HistoryInfo.Use historyInfo = useService.findHistory(request.getMemberId(), request.getTransactionId());
            if (historyInfo == null) {
                throw new CustomException(ResponseCode.NO_HISTORY);
            }

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
                    request.getTransactionId(),
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
                    request.getTransactionId(),
                    request.getPoint(),
                    cancelableAmount,
                    request.getDescription(),
                    useDetailList
            );
        }

        return UseCancelResponse.builder()
                .balance(useService.findBalance(request.getMemberId()))
                .transactionId(request.getTransactionId())
                .build();
    }
}
