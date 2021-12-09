package com.mkubica.managementservice.transaction;

import com.mkubica.managementservice.domain.dto.GatewayModel;
import com.mkubica.managementservice.service.ip.IpAssigner;
import io.vavr.control.Try;

public class TransactionalIpAssigner implements Transactional<GatewayModel, Try<GatewayModel>> {

    private final IpAssigner ipAssigner;
    private GatewayModel provided;

    public TransactionalIpAssigner(IpAssigner ipAssigner) {
        this.ipAssigner = ipAssigner;
    }

    @Override
    public Try<GatewayModel> execute(GatewayModel model) {
        this.provided = model;
        return ipAssigner
                .assignIp(model.getCommonName(), model.getIpAddress())
                .map(assignedIp -> GatewayModel.builder()
                        .withCommonName(model.getCommonName())
                        .withCertificate(model.getCertificate())
                        .withPrivateKey(model.getPrivateKey())
                        .withIpAddress(assignedIp)
                        .build());
    }

    @Override
    public Try<GatewayModel> rollback() throws RuntimeException {
        return ipAssigner
                .revokeIp(provided.getCommonName())
                .map(vo -> provided);
    }
}
