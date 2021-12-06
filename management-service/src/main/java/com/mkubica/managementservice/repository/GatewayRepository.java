/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.repository;

import com.mkubica.managementservice.domain.dao.GatewayEntity;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.vavr.control.Option;

@Repository
@Transactional
public interface GatewayRepository extends CrudRepository<GatewayEntity, String> {

    Option<GatewayEntity> getGatewayEntityByCommonName(String commonName);
    Option<GatewayEntity> getGatewayEntityByIpAddress(String ipAddress);
    void deleteByCommonName(String commonName);
}
