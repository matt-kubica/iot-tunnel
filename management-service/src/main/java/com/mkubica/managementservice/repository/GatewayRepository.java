/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.repository;

import com.mkubica.managementservice.domain.dao.GatewayEntity;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import io.vavr.control.Option;

import java.util.List;


@Repository
@Transactional
public interface GatewayRepository extends JpaRepository<GatewayEntity, String> {

    @NonNull
    List<GatewayEntity> findAll();
    Option<GatewayEntity> getGatewayEntityByCommonName(String commonName);
    Option<GatewayEntity> getGatewayEntityByIpAddress(String ipAddress);

}
