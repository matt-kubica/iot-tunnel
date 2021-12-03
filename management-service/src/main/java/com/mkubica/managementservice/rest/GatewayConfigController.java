/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.rest;

import com.mkubica.managementservice.service.GatewayService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GatewayConfigController {

    private final GatewayService gatewayService;

    @GetMapping("/gateway-config/{common-name}")
    public void get(@PathVariable("common-name") String commonName, HttpServletResponse response) {
        try {
            String config = gatewayService.getGatewayConfig(commonName).makeString();
            FileCopyUtils.copy(new ByteArrayInputStream(config.getBytes()), response.getOutputStream());
            response.setContentType("application/file");
            response.flushBuffer();
        } catch (IOException exception) {
            log.error("Error writing file to output stream");
        }
    }
}
