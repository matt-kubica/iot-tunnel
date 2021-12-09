package com.mkubica.managementservice.transaction;

public interface Transactional<P, R> {
    R execute(P param);
    R rollback();
}
