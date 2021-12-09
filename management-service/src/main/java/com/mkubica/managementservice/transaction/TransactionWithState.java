package com.mkubica.managementservice.transaction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TransactionWithState<T> {

    private final Transaction<T> transaction;
    private final T state;
}
