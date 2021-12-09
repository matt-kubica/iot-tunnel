package com.mkubica.managementservice.transaction;

import io.vavr.collection.Seq;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TransactionChainExecutor {

    public <T> Try<T> executeChain(T inputModel, TransactionChain<T> transactionChain) {
        return Try.of(() -> transactionChain.getDefinedTransactions().foldLeft(inputModel,
                (dto, transaction) -> transaction
                        .getExecutionRoutine().execute(dto)
                        .onSuccess(res -> transactionChain.appendSuccessfulTransactionWithState(transaction, dto))
                        .onFailure(cause -> rollbackRoutine(cause, transactionChain.getSuccessfulTransactionsWithStates()))
                        .get())
        );
    }

    private <T> void rollbackRoutine(Throwable cause, Seq<TransactionWithState<T>> successfulTransactions) {
        log.error("Transaction chain broken! Caused by: {}", cause.toString());
        log.error("Rolling back transaction chain:");
        successfulTransactions.forEach(tuple -> log.error("\t-> Transaction{}(pure = {}) -> {}",
                tuple.getTransaction().hashCode(), tuple.getTransaction().isPure(), tuple.getState().toString()));

        successfulTransactions.reverse().forEach(tuple ->
                tuple.getTransaction().getRollbackRoutine().rollback(tuple.getState())
        );
    }

}
