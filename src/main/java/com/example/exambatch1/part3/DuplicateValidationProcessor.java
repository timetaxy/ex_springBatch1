package com.example.exambatch1.part3;

import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DuplicateValidationProcessor<T> implements ItemProcessor<T, T> {

    private final Map<String, Object> keyPool = new ConcurrentHashMap<>();
    private final Function<T, String> keyExtractor;
    private final boolean allowDuplicate;

    public DuplicateValidationProcessor(Function<T, String> keyExtractor,
                                        boolean allowDuplicate) {

        this.keyExtractor = keyExtractor;
        this.allowDuplicate = allowDuplicate;
    }

    @Override
    public T process(T item) throws Exception {
//        필터링 안함
        if (allowDuplicate) {
            return item;
        }

        String key = keyExtractor.apply(item);
//키풀에 존재하는 키인지 확, 존재하면 중복이므로 null 리턴
        if (keyPool.containsKey(key)) {
            return null;
        }

        keyPool.put(key, key);
        return item;
    }
}