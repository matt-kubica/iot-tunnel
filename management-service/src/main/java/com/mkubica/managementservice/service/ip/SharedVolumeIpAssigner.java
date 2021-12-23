/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service.ip;

import com.mkubica.managementservice.domain.dao.GatewayEntity;
import com.mkubica.managementservice.exception.IpAddressNotUniqueException;
import com.mkubica.managementservice.exception.IpAddressNotWithinPoolException;
import com.mkubica.managementservice.exception.IpAddressPoolFullyAllocatedException;
import com.mkubica.managementservice.repository.GatewayRepository;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

import inet.ipaddr.IPAddress;

import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;
import io.vavr.collection.SortedSet;
import io.vavr.collection.Stream;
import io.vavr.collection.TreeSet;

import io.vavr.control.Try;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static inet.ipaddr.Address.ADDRESS_LOW_VALUE_COMPARATOR;
import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static java.lang.String.format;

public class SharedVolumeIpAssigner implements IpAssigner {

    private final String ccdPath;
    private final GatewayRepository gatewayRepository;

    private final StatefulSortedSet<IPAddressSeqRange> allocatedIpPairs;
    private final StatefulSortedSet<IPAddressSeqRange> ipPairsPool;


    // TODO: add some validation, here or on bean creation - to validate ccd path and internal address pool
    public SharedVolumeIpAssigner(
            String ccdPath,
            GatewayRepository gatewayRepository,
            String internalNetworkAddress,
            String internalNetworkMask
    ) {
        this.ccdPath = ccdPath;
        this.gatewayRepository = gatewayRepository;

        // NOTE: when creating pool, omit first pair since it is allocated for vpn server
        this.ipPairsPool = StatefulSortedSet.of(() -> IpAssigner
                .getAllPairsFromAddressAndMask(internalNetworkAddress, internalNetworkMask)
                .get()
                .drop(1)
        );
        this.allocatedIpPairs = StatefulSortedSet.of(this::getAllAllocatedPairs);
    }


    @Override
    @NonNull
    public Try<String> assignIp(@NonNull String commonName, @Nullable String ipAddressString) {
        Try<IPAddressSeqRange> chosenIpPair = ipAddressString == null
                ? chooseIpPair()
                : Try.success(ipAddressString)
                    .flatMap(this::checkIfIpWithinPool)
                    .flatMap(this::checkIfIpAlreadyAssigned)
                    .flatMap(IpAssigner::seqRangeFromString);
        return assignChosenIpPair(commonName, chosenIpPair);
    }

    @Override
    @NonNull
    public Try<String> revokeIp(@NonNull String commonName) {
        return deleteIpConfigFile(commonName)
                .map(ipAddress -> ipAddress.setPrefixLength(31).toSequentialRange())
                .map(seqRange -> allocatedIpPairs.remove(seqRange).getLower().toCanonicalString());
    }


    private Try<String> assignChosenIpPair(String commonName, Try<IPAddressSeqRange> pair) {
        return pair
                .flatMap(seqRange -> saveIpConfigFile(commonName, seqRange))
                .map(ipAddress -> ipAddress.setPrefixLength(31).toSequentialRange())
                .map(seqRange -> allocatedIpPairs.add(seqRange).getLower().toCanonicalString());
    }

    private TreeSet<IPAddressSeqRange> getAllAllocatedPairs() {

        return TreeSet.ofAll(ADDRESS_LOW_VALUE_COMPARATOR,
                // NOTE: repository returns java stream (since JPA is not compatible with vavr stream),
                // thus it needs to be converted
                Stream.ofAll(gatewayRepository.findAll())
                    .map(GatewayEntity::getIpAddress)
                    .map(ipAddress -> IpAssigner.seqRangeFromString(ipAddress).toOption().getOrNull())
                    .filter(Objects::nonNull)
        );
    }

    // NOTE: it seems that warning from method below is caused by some bug in intelliJ
    // https://github.com/vavr-io/vavr/issues/2411
    private Try<IPAddressSeqRange> chooseIpPair() {
        return Try.of(() -> allocatedIpPairs.all().isEmpty()
                    ? ipPairsPool.all().get()
                    : ipPairsPool.all().find(ip -> !allocatedIpPairs.all().contains(ip)).get())
                .mapFailure(Case($(instanceOf(NoSuchElementException.class)), x ->
                        new IpAddressPoolFullyAllocatedException()));
    }

    private Try<String> checkIfIpWithinPool(String ipAddress) {
        return IpAssigner.seqRangeFromString(ipAddress)
                .map(ipPairsPool.all()::contains)
                .flatMap(res -> res
                        ? Try.success(ipAddress)
                        : Try.failure(new IpAddressNotWithinPoolException(
                            format("Ip address '%s' doesn't belong to the pool: (<%s> ; <%s>)",
                                    ipAddress, ipPairsPool.all().get(),
                                    ipPairsPool.all().last())))
                );
    }

    private Try<String> checkIfIpAlreadyAssigned(String ipAddress) {
        return gatewayRepository.getGatewayEntityByIpAddress(ipAddress).isEmpty()
                ? Try.success(ipAddress)
                : Try.failure(new IpAddressNotUniqueException(
                format("Ip address '%s' already allocated", ipAddress)));
    }

    private Try<IPAddress> saveIpConfigFile(String commonName, IPAddressSeqRange ipAddressPair) {
        return Try.of(() -> createFile(new File(ccdPath, commonName)))
                .mapTry(fw -> writeIpPairToFile(fw, ipAddressPair));
    }

    private Try<IPAddress> deleteIpConfigFile(String commonName) {
        return Try.of(() -> new File(ccdPath, commonName).delete())
                .flatMap(res -> res
                            ? gatewayRepository.getGatewayEntityByCommonName(commonName).toTry()
                            : Try.failure(new FileNotFoundException()))
                .map(GatewayEntity::getIpAddress)
                .map(IPAddressString::new)
                .mapTry(IPAddressString::getAddress);
    }

    private static FileWriter createFile(File file) throws IOException {
        if (file.createNewFile()) {
            return new FileWriter(file);
        }
        throw new FileAlreadyExistsException(format("File %s already exist", file.getAbsolutePath()));
    }

    private static IPAddress writeIpPairToFile(FileWriter fw, IPAddressSeqRange ipAddressPair) throws IOException {
        fw.write(format("ifconfig-push %s\n", IpAssigner.seqRangeToString(ipAddressPair).get()));
        fw.close();
        return ipAddressPair.getLower();
    }

    private static class StatefulSortedSet<T> {

        private SortedSet<T> collection;

        private StatefulSortedSet(SortedSet<T> collection) {
            this.collection = collection;
        }

        public static <T> StatefulSortedSet<T> of(Supplier<SortedSet<T>> supplier) {
            return new StatefulSortedSet<>(supplier.get());
        }

        public T add(T object) {
            this.collection = collection.add(object);
            return object;
        }

        public T remove(T object) {
            this.collection = collection.remove(object);
            return object;
        }

        public SortedSet<T> all() {
            return this.collection;
        }
    }
}
