package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.BusinessException;
import io.hhplus.tdd.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PointServiceImpl implements PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    @Override
    public UserPoint retrieve(long userId) {
        if (userId < 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserPoint userPoint = userPointTable.selectById(userId);
        return userPoint;
    }

    @Override
    public List<PointHistory> retrieveHistory(long userId) {
        if (userId < 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(userId);
        return pointHistories;
    }

    public UserPoint charge(long userId, long amount) {
        UserPoint userPoint;

        synchronized (this) {
            userPoint = userPointTable.selectById(userId);
            userPoint = userPoint.charge(amount);

            userPointTable.insertOrUpdate(userPoint.id(), userPoint.point());
        }

        pointHistoryTable.insert(
            userId,
            amount,
            TransactionType.CHARGE,
            System.currentTimeMillis()
        );

        return userPoint;
    }

    public UserPoint use(long userId, long amount) {
        UserPoint userPoint = userPointTable.selectById(userId)
            .use(amount);
        log.info("userPoint: {}", userPoint);
        userPointTable.insertOrUpdate(userPoint.id(), userPoint.point());

        pointHistoryTable.insert(
            userId,
            amount,
            TransactionType.USE,
            System.currentTimeMillis()
        );

        return userPoint;
    }

}
