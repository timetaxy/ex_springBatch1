package com.example.exambatch1.part6;

import com.example.exambatch1.part4.UserRepository;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

// 파티션 스텝 튜닝
public class UserLevelUpPartitioner implements Partitioner {
    private final UserRepository userRepository;

    public UserLevelUpPartitioner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        //gridSize 슬레이브 스텝의 사이즈
        //배치처리 대상 데이터 중 id 기준 최소 최대 구해야 함
        long minId = userRepository.findMinId(); // 1
        long maxId = userRepository.findMaxId(); // 40,000

        //각 슬레이브 스텝에서 계산할 아이템 갯수
        long targetSize = (maxId - minId) / gridSize + 1; // 5000
        // (40000-1)/8+1

        /**
         * partition0 : 1, 5000
         * partition1 : 5001, 10000
         * ...
         * partition7 : 35001, 40000
         */
        Map<String, ExecutionContext> result = new HashMap<>();

        long number = 0;

        long start = minId;

        long end = start + targetSize - 1;

        while (start <= maxId) {
            ExecutionContext value = new ExecutionContext();

            result.put("partition" + number, value);

            if (end >= maxId) {
                end = maxId;
            }

            value.putLong("minId", start);
            value.putLong("maxId", end);

            start += targetSize;
            end += targetSize;
            number++;
        }

        return result;
    }
}
