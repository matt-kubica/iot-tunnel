/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.rest;

import com.mkubica.managementservice.domain.dto.GatewayModel;
import com.mkubica.managementservice.domain.dto.GatewaySimplifiedModel;
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
        return gatewayService.getGateway(commonName)
                .onFailure(exc -> log.error("Error getting gateway entity:\n" + exc))
                .get();
    }

    //    @GetMapping("/gateway")
    //    public List<GatewayModel> get() {
    //        return Stream.of(gatewayRepository.getAll())
    //                .map(GatewayModel::from)
    //                .collect(Collectors.toList());
    //    }

    @PostMapping("/gateway")
    public GatewayModel post(@RequestBody GatewaySimplifiedModel model) {
        return gatewayService.createGateway(model)
                .onFailure(exc -> log.error("Error posting gateway entity: {}", exc.toString()))
                .onSuccess(val -> log.debug("Successfully posted entity: {}", val))
                .get();
    }

    @DeleteMapping("/gateway/{common-name}")
    public void delete(@PathVariable("common-name") String commonName) {
        gatewayService.deleteGateway(commonName);
    }
}
