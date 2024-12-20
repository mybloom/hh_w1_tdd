package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {

    UserPoint charge(long userId, long amount);
    UserPoint use(long userId, long amount);

    UserPoint retrieve(long userId);

    List<PointHistory> retrieveHistory(long userId);
}
