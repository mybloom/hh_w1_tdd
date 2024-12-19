package io.hhplus.tdd.point;

import io.hhplus.tdd.exception.BusinessException;
import io.hhplus.tdd.exception.ErrorCode;

public record UserPoint(
    long id,
    long point,
    long updateMillis
) {

    public UserPoint(long id, long point, long updateMillis) {
        this.id = id;
        this.point = point;
        this.updateMillis = updateMillis;
    }

    public static final int MIN_POINT = 1;
    public static final int MAX_POINT = 10_000;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint charge(long amount) {
        if (amount < MIN_POINT) {
            throw new BusinessException(ErrorCode.POINT_WITHIN_LIMIT);
        }
        if (amount > MAX_POINT) {
            throw new BusinessException(ErrorCode.POINT_EXCEED_LIMIT);
        }

        return new UserPoint(id, point + amount, System.currentTimeMillis());
    }

    public UserPoint use(long amount) {
        if (this.point < amount) {
            throw new BusinessException(ErrorCode.POINTS_INSUFFICIENT);
        }

        return new UserPoint(id, this.point - amount, System.currentTimeMillis());
    }
}
