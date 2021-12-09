package com.mkubica.managementservice.transaction;

import com.mkubica.managementservice.domain.dto.GatewayModel;
import com.mkubica.managementservice.exception.CommonNameNotUniqueException;
import com.mkubica.managementservice.exception.IpAddressNotUniqueException;
import com.mkubica.managementservice.repository.GatewayRepository;
import io.vavr.control.Try;

import static java.lang.String.format;

public class TransactionalGatewayModelValidator implements Transactional<GatewayModel, Try<GatewayModel>> {

    private final GatewayRepository gatewayRepository;
    private GatewayModel provided;

    public TransactionalGatewayModelValidator(GatewayRepository gatewayRepository) {
        this.gatewayRepository = gatewayRepository;
    }

    @Override
    public Try<GatewayModel> execute(GatewayModel model) {
        this.provided = model;
        return Try.of(() -> GatewayModel.builder()
                .withCommonName(validateCommonName(model).get().getCommonName())
                .withIpAddress(validateIpAddress(model).get().getIpAddress())
                .withPrivateKey(model.getPrivateKey())
                .withCertificate(model.getCertificate())
                .build());
    }

    @Override
    public Try<GatewayModel> rollback() throws RuntimeException {
        return Try.success(provided);
    }

    private Try<GatewayModel> validateCommonName(GatewayModel model) {
        return gatewayRepository.getGatewayEntityByCommonName(model.getCommonName()).isEmpty()
                ? Try.success(model)
                : Try.failure(new CommonNameNotUniqueException(
                format("Gateway with common name '%s' already exist", model.getCommonName())));
    }

    private Try<GatewayModel> validateIpAddress(GatewayModel model) {
        return gatewayRepository.getGatewayEntityByIpAddress(model.getIpAddress()).isEmpty()
                ? Try.success(model)
                : Try.failure(new IpAddressNotUniqueException(
                format("Ip address '%s' already allocated", model.getIpAddress())));
    }
}
