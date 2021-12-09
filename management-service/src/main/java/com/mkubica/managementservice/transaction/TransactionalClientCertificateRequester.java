package com.mkubica.managementservice.transaction;

import com.mkubica.managementservice.domain.dto.GatewayModel;
import com.mkubica.managementservice.service.cert.ClientCertificateRequester;
import io.vavr.control.Try;

public class TransactionalClientCertificateRequester implements Transactional<GatewayModel, Try<GatewayModel>> {

    private final ClientCertificateRequester clientCertificateRequester;
    private GatewayModel provided;

    public TransactionalClientCertificateRequester(ClientCertificateRequester clientCertificateRequester) {
        this.clientCertificateRequester = clientCertificateRequester;
    }

    @Override
    public Try<GatewayModel> execute(GatewayModel model) {
        this.provided = model;
        return clientCertificateRequester
                .requestBundle(model.getCommonName())
                .map(bundle -> GatewayModel.builder()
                        .withCertificate(bundle.getCertificate())
                        .withPrivateKey(bundle.getPrivateKey())
                        .withCommonName(model.getCommonName())
                        .withIpAddress(model.getIpAddress())
                        .build());
    }

    @Override
    public Try<GatewayModel> rollback() throws RuntimeException {
        // revoke certificate
        return Try.success(provided);
    }
}
