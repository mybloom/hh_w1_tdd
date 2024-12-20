package io.hhplus.tdd.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.hhplus.tdd.exception.BusinessException;
import io.hhplus.tdd.exception.ErrorCode;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;

public class UsePointTest {

    private static final long USER_ID = 1L;

    @Test
    void 초기_UserPoint는_0_point를_가진다() {
        //given
        UserPoint userPoint = UserPoint.empty(USER_ID);

        assertThat(userPoint)
            .usingRecursiveComparison()
            .ignoringFields("updateMillis")
            .isEqualTo(new UserPoint(USER_ID, 0L, 0L));
    }

    @Test
    void 초기_UserPoint_1000_충전하면_1000_된다() {
        //given
        UserPoint userPoint = UserPoint.empty(USER_ID);

        //when
        userPoint = userPoint.charge(1000L);

        //then
        assertThat(userPoint)
            .usingRecursiveComparison()
            .ignoringFields("updateMillis")
            .isEqualTo(new UserPoint(USER_ID, 1000L, 0L));
    }

    @Test
    void 충전금액이_최소값보다_작으면_실패한다() {
        //given
        UserPoint userPoint = UserPoint.empty(USER_ID);

        assertThatThrownBy(() -> userPoint.charge(UserPoint.MIN_POINT - UserPoint.MIN_POINT))
            .isInstanceOf(BusinessException.class)
            .hasMessage(ErrorCode.POINT_WITHIN_LIMIT.getMessage());
    }

    @Test
    void 충전금액이_최대값보다_크면_실패한다() {
        //given
        UserPoint userPoint = UserPoint.empty(USER_ID);

        assertThatThrownBy(() -> userPoint.charge(UserPoint.MAX_POINT + UserPoint.MIN_POINT))
            .isInstanceOf(BusinessException.class)
            .hasMessage(ErrorCode.POINT_EXCEED_LIMIT.getMessage());
    }

    @Test
    void 사용한_포인트만큼_차감된다() {
        //given
        UserPoint userPoint = UserPoint.empty(USER_ID)
            .charge(1000L);

        //when
        UserPoint beforeUserPoint = userPoint.use(200L);

        //then
        assertThat(beforeUserPoint)
            .usingRecursiveComparison()
            .ignoringFields("updateMillis")
            .isEqualTo(new UserPoint(USER_ID, 800L, 0L));
    }

    @Test
    void 보유포인트_초과_사용시_실패한다() {
        //given
        UserPoint userPoint = UserPoint.empty(USER_ID)
            .charge(1000L);

        //then
        assertThatThrownBy(() -> userPoint.use(2000L))
            .isInstanceOf(BusinessException.class)
            .hasMessage(ErrorCode.POINTS_INSUFFICIENT.getMessage());
    }

    @Test
    void 총_포인트_조회() {
        //given
        UserPoint userPoint = UserPoint.empty(USER_ID);

        //when
        userPoint = userPoint
            .charge(1000L)
            .use(200L);

        //then
        assertThat(userPoint.point())
            .isEqualTo(800L);
    }

}
