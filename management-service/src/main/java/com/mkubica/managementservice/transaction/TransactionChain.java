package com.mkubica.managementservice.transaction;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
public class TransactionChain<T> {

    private final Seq<Transaction<T>> definedTransactions;
    private Seq<TransactionWithState<T>> successfulTransactionsWithStates;

    @SafeVarargs
    public static <T> TransactionChain<T> of(Transaction<T>... transactions) {
        return new TransactionChain<>(transactions);
    }

    @SafeVarargs
    private TransactionChain(Transaction<T>... transactions) {
        this.definedTransactions = List.of(transactions);
        this.successfulTransactionsWithStates = List.empty();
    }

    public void appendSuccessfulTransactionWithState(Transaction<T> transaction, T state) {
        successfulTransactionsWithStates
                = successfulTransactionsWithStates.append(new TransactionWithState<>(transaction, state));
    }
}
