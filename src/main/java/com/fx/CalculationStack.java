package com.fx;

import lombok.extern.slf4j.Slf4j;

import java.util.Stack;

/**
 * ClassName: CalculationStack
 * Package: com.fx
 * Description:
 * Create: 2024/4/26 - 23:57
 *
 * @author : Fang-
 * @version : v1.0
 */
@Slf4j
public class CalculationStack<E> extends Stack<E> {
    @Override
    public E push(E item) {
        log.debug("push: {}", item);
        E element = super.push(item);
        log.debug("after push, Stack={}", this);
        return element;
    }

    @Override
    public synchronized E pop() {
        E element = super.pop();
        log.debug("pop: {}", element);
        log.debug("after pop, Stack={}", this);
        return element;
    }
}
