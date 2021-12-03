/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.repository;

import com.mkubica.managementservice.domain.dao.GatewayEntity;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface GatewayRepository extends CrudRepository<GatewayEntity, String> {

    Optional<GatewayEntity> getGatewayEntityByCommonName(String commonName);
    Optional<GatewayEntity> getGatewayEntityByIpAddress(String ipAddress);
    void deleteByCommonName(String commonName);
}
