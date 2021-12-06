/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.rest;

import com.mkubica.managementservice.domain.dto.GatewayConfigModel;
import com.mkubica.managementservice.service.GatewayService;

import java.io.ByteArrayInputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GatewayConfigController {

    private final GatewayService gatewayService;

    @GetMapping("/gateway-config/{common-name}")
    public void get(@PathVariable("common-name") String commonName, HttpServletResponse response) {
        gatewayService.getGatewayConfig(commonName)
                .map(GatewayConfigModel::makeString)
                .map(config -> new ByteArrayInputStream(config.getBytes()))
                .andThenTry(config -> {
                    FileCopyUtils.copy(config, response.getOutputStream());
                    response.setContentType("application/file");
                    response.flushBuffer();
                })
                .onFailure(exc -> log.error("Error obtaining config:\n" + exc.getMessage()))
                .onSuccess(val -> log.info("Config successfully downloaded!"));
    }
}
