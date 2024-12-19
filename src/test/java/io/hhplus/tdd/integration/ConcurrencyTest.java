package io.hhplus.tdd.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.hhplus.tdd.point.PointService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointService pointService;


    @Test
    void 동시_충전_요청_후_최종_포인트_확인() throws Exception {
        // Given
        long userId = 1L;
        long initialPoint = 0L;
        long chargeAmount = 1L;
        int chargeTimes = 30; // 동시 요청 횟수
        long expectedFinalPoint = initialPoint + chargeAmount * chargeTimes;

        // When: 동시 요청 처리
        ExecutorService executorService = Executors.newFixedThreadPool(chargeTimes);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < chargeTimes; i++) {
            tasks.add(() -> {
                mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargeAmount)))
                    .andExpect(status().isOk());
                return null;
            });
        }
        executorService.invokeAll(tasks);
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        // Then: 최종 포인트 값 확인
        mockMvc.perform(get("/point/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.point").value(expectedFinalPoint));  // 포인트 값 확인
    }

}
