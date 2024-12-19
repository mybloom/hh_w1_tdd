package io.hhplus.tdd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.BusinessException;
import io.hhplus.tdd.exception.ErrorCode;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointServiceImpl;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointServiceImpl pointService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 충전_정상적인_포인트_충전_성공() {
        // given
        long userId = 1L;
        long currentPoint = 100L;
        long chargePoint = 1_000L;

        UserPoint currentUserPoint = new UserPoint(userId, currentPoint, 0L);
        when(userPointTable.selectById(userId))
            .thenReturn(currentUserPoint);
        when(pointHistoryTable.insert(userId, chargePoint, TransactionType.CHARGE,
            System.currentTimeMillis()))
            .thenReturn(new PointHistory(1L, userId, chargePoint, TransactionType.CHARGE, 0L));

        //when
        UserPoint chargedUserPoint = pointService.charge(userId, chargePoint);

        // Then
        assertThat(chargedUserPoint)
            .usingRecursiveComparison()
            .ignoringFields("updateMillis")
            .isEqualTo(new UserPoint(userId, currentPoint + chargePoint, 0L));

        verify(pointHistoryTable).insert(eq(userId), eq(chargePoint), eq(TransactionType.CHARGE),
            anyLong());
    }

    @Test
    void 사용_정상적인_포인트_사용_성공() {
        //given
        long userId = 1L;
        long currentPoint = 1000L;
        long usePoint = 200L;

        //Mock behavior
        when(userPointTable.selectById(userId))
            .thenReturn(new UserPoint(userId, currentPoint, 0L));
        when(pointHistoryTable.insert(userId, usePoint, TransactionType.USE,
            System.currentTimeMillis()))
            .thenReturn(new PointHistory(1L, userId, usePoint, TransactionType.USE, 0L));

        //when
        UserPoint userPoint = pointService.use(userId, usePoint);

        //then
        assertThat(userPoint)
            .usingRecursiveComparison()
            .ignoringFields("updateMillis")
            .isEqualTo(new UserPoint(userId, currentPoint - usePoint, 0L));

        verify(pointHistoryTable).insert(eq(userId), eq(usePoint), eq(TransactionType.USE),
            anyLong());
    }

    @Test
    void 사용_보유포인트_초과_사용_실패() {
        //given
        long userId = 1L;
        long currentPoint = 200L;
        long usePoint = 1000L;

        //Mock behavior
        when(userPointTable.selectById(userId))
            .thenReturn(new UserPoint(userId, currentPoint, 0L));

        //when
        assertThatThrownBy(() -> pointService.use(userId, usePoint))
            .isInstanceOf(BusinessException.class)
            .hasMessage(ErrorCode.POINTS_INSUFFICIENT.getMessage());

        //then
        when(pointHistoryTable.selectAllByUserId(userId))
            .thenReturn(Collections.emptyList());
    }

    @Test
    void 조회_특정_유저의_포인트_조회_성공() {
        //given
        long userId = 1L;
        long currentPoint = 1000L;

        UserPoint currentUserPoint = new UserPoint(userId, currentPoint, 0L);
        when(userPointTable.selectById(userId))
            .thenReturn(currentUserPoint);

        //when
        UserPoint userPoint = pointService.retrieve(userId);

        //then
        assertThat(userPoint)
            .usingRecursiveComparison()
            .ignoringFields("updateMillis")
            .isEqualTo(new UserPoint(userId, currentPoint, 0L));
    }

    @Test
    void 조회_존재하지않는_유저의_포인트_조회_실패() {
        //given
        long userId = 0L;
        long currentPoint = 1000L;

        UserPoint currentUserPoint = new UserPoint(userId, currentPoint, 0L);
        when(userPointTable.selectById(userId))
            .thenReturn(currentUserPoint);

        //when
        assertThatThrownBy(() -> pointService.retrieve(userId))
            .isInstanceOf(BusinessException.class)
            .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void 내역_포인트내역_조회_성공() {
        //given
        long userId = 1L;
        long chargePoint = 1000L;
        long usePoint = 500L;

        List<PointHistory> mockHistory = Arrays.asList(
            new PointHistory(1L, userId, chargePoint, TransactionType.CHARGE, System.currentTimeMillis()),
            new PointHistory(2L, userId, usePoint, TransactionType.USE, System.currentTimeMillis())
        );
        when(pointHistoryTable.selectAllByUserId(userId))
            .thenReturn(mockHistory);

        //when
        List<PointHistory> pointHistories = pointService.retrieveHistory(userId);

        //then
        assertThat(pointHistories)
            .isNotNull()
            .hasSize(2)
            .extracting(PointHistory::userId)
            .containsOnly(userId);
    }

}
