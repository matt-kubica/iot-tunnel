package com.mkubica.managementservice.transaction;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Try;

public class ChainExecutor<T> {
    
    @SafeVarargs
    public final Try<T> execute(T inputModel, Transactional<T, Try<T>>... chain) {
        var result = inputModel;
        List<Tuple2<Transactional<T, Try<T>>, Throwable>> issues = List.empty();
        for (var transaction : chain) {
            var output = transaction.execute(result);
            output.onFailure(exc -> issues.append(Tuple.of(transaction, exc)));
            if (output.isFailure()) rollbackRoutine(issues);
            result = output.get();
        }
        return Try.success(result);
    }

    private void rollbackRoutine(List<Tuple2<Transactional<T, Try<T>>, Throwable>> issues) {
        for (var i : issues.reverse()) {
            var output = i._1.rollback();
            if (output.isFailure()) throw new RuntimeException("Fatal exception, cannot rollback!");
        }
    }

}
