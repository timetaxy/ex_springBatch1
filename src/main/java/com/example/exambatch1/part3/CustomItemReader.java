package com.example.exambatch1.part3;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CustomItemReader<T> implements ItemReader<T> {
    private final List<T> items;

//    public  CustomItemReader(List<T> items){
//        this.items = new ArrayList<>(items);
//    }
    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!items.isEmpty()) {
            return items.remove(0);
        }
        return null;
    }
}
// 동작원리 이해 위해 스프링 제공 ListItemReader 동일하게 구현 해봄