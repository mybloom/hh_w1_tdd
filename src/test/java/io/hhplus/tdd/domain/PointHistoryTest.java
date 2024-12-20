package io.hhplus.tdd.domain;

import static org.assertj.core.api.Assertions.*;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import org.junit.jupiter.api.Test;


public class PointHistoryTest {

    @Test
    void 포인트_내역_조회() {
        // given
        long userId = 1L;
        long amount = 200L;
        TransactionType type = TransactionType.CHARGE;
        long updateMillis = System.currentTimeMillis();

        PointHistory pointHistory = new PointHistory(1L, userId, amount, type, updateMillis);

        // when & then
        assertThat(pointHistory.amount()).isEqualTo(amount);
        assertThat(pointHistory.type()).isEqualTo(type);
        assertThat(pointHistory.userId()).isEqualTo(userId);
        assertThat(pointHistory.type()).isEqualTo(TransactionType.CHARGE);
    }

}
