/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.rest;

import com.mkubica.managementservice.domain.dto.GatewayModel;
import com.mkubica.managementservice.service.GatewayService;

import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class GatewayController {

    private final GatewayService gatewayService;

    @GetMapping("/gateway/{common-name}")
    public GatewayModel get(@PathVariable("common-name") String commonName) {
        return gatewayService.getGateway(GatewayModel.builder().withCommonName(commonName).build())
                .onFailure(exc -> log.error("Error when getting gateway entity with common-name:{}", commonName))
                .onSuccess(res -> log.debug("Successfully obtained entity: {}", res))
                .get();
    }

    @PostMapping("/gateway")
    public GatewayModel post(@RequestBody GatewayModel model) {
        return gatewayService.createGateway(model)
                .onFailure(exc -> log.error("Error when posting gateway entity with common-name:{}", model.getCommonName()))
                .onSuccess(res -> log.debug("Successfully created entity: {}", res))
                .get();
    }

    @DeleteMapping("/gateway/{common-name}")
    public GatewayModel delete(@PathVariable("common-name") String commonName) {
        return gatewayService.deleteGateway(GatewayModel.builder().withCommonName(commonName).build())
                .onFailure(exc -> log.error("Error when deleting gateway entity with common-name:{}", commonName))
                .onSuccess(res -> log.debug("Successfully deleted entity: {}", res))
                .get();
    }
}
