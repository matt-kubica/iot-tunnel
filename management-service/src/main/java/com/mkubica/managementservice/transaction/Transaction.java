package com.mkubica.managementservice.transaction;

import io.vavr.control.Try;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Transaction<T> {

    private final ExecutionRoutine<T> executionRoutine;
    private final RollbackRoutine<T> rollbackRoutine;
    private final boolean pure;
    
    public static <T> Transaction<T> dirty(ExecutionRoutine<T> executionRoutine, RollbackRoutine<T> rollbackRoutine) {
        return new Transaction<>(executionRoutine, rollbackRoutine, false);
    }

    public static <T> Transaction<T> pure(ExecutionRoutine<T> executionRoutine) {
        return new Transaction<>(executionRoutine, m -> Try.success(null), true);
    }

    public static <T> Transaction<T> conditional(boolean condition, Transaction<T> transaction) {
        return condition ? transaction : empty();
    }

    public static <T> Transaction<T> alternative(boolean condition, Transaction<T> onTrue, Transaction<T> onFalse) {
        return condition ? onTrue : onFalse;
    }

    public static <T> Transaction<T> empty() {
        return new Transaction<>(Try::success, m -> Try.success(null), true);
    }

    public interface ExecutionRoutine<T> {
        Try<T> execute(T dto);
    }

    public interface RollbackRoutine<T> {
        Try<Void> rollback(T dto);
    }
}
