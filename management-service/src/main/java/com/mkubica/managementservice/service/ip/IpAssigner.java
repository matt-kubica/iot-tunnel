/* Copyright 2021 Mateusz Kubica */
package com.mkubica.managementservice.service.ip;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;
import io.vavr.collection.TreeSet;
import io.vavr.control.Try;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.stream.Collectors;

import static java.lang.String.format;


/**
 * Interface whose implementations should somehow communicate
 * with vpn server and allocate ip addresses for certain common names.
 */
public interface IpAssigner {


    /**
     * Method which associates common name with ip address
     * by performing procedures hidden behind specific implementation.
     * This method shouldn't validate common name, but should check whether ip address can be assigned,
     * it doesn't not need to cover ip string string validation.
     *
     * If {@param ipAddressString} is null, ip address will be assigned automatically
     * for this particular common name.
     *
     * @param commonName - common name of gateway to which ip address will be assigned
     * @param ipAddressString - valid ip address string with even, non-zero suffix, ex. 10.8.0.2, 10.8.0.4, ..., or null
     *
     * @return assigned ip address string or exception, wrapped with {@link Try}
     *
     */
    @NonNull
    Try<String> assignIp(@NonNull String commonName, @Nullable String ipAddressString);


    /**
     * Alias for {@link #assignIp(String, String) assignIp("some-common-name", null)}
     *
     * @param commonName - common name of gateway to which ip address will be assigned
     *
     * @return assigned ip address string or exception, wrapped with {@link Try}
     */
    @NonNull
    default Try<String> assignIp(@NonNull String commonName) {
        return assignIp(commonName, null);
    }


    /**
     * Method which removes association between provided common name and assigned ip address,
     * it shouldn't validate common name.
     *
     * @param commonName - common name of gateway from which ip address will be revoked
     *
     * @return revoked ip address string or exception, wrapped with {@link Try}
     */
    @NonNull
    Try<String> revokeIp(@NonNull String commonName);

    /**
     * Static method which converts CIDR (eg. 10.8.0.0/16) to {@link TreeSet}
     * of {@link IPAddressSeqRange} with 31 prefix length.
     *
     * @param cidr - valid CIDR
     * @return {@link TreeSet<IPAddressSeqRange>} or exception, wrapped with {@link Try}
     */
    static Try<TreeSet<IPAddressSeqRange>> getAllPairsFromCidr(String cidr) {
        return Try.of(() -> new IPAddressString(cidr).toAddress())
                .map(network -> network.setPrefixLength(31, false).prefixBlockStream())
                .map(stream -> stream.map(IPAddress::toSequentialRange))
                .map(TreeSet::ofAll);

    }

    /**
     * Static method which converts address and mask pair to {@link TreeSet}
     * of {@link IPAddressSeqRange} with 31 prefix length.
     *
     * @param address - valid network address, eg. 10.8.0.0
     * @param mask - valid network mask, eg. 255.255.0.0
     * @return {@link TreeSet<IPAddressSeqRange>} or exception, wrapped with {@link Try}
     */
    static Try<TreeSet<IPAddressSeqRange>> getAllPairsFromAddressAndMask(String address, String mask) {
        return Try.of(() -> new IPAddressString(format("%s/%s", address, mask)).toAddress())
                .map(network -> network.setPrefixLength(31, false).prefixBlockStream())
                .map(stream -> stream.map(IPAddress::toSequentialRange))
                .map(TreeSet::ofAll);

    }

    /**
     * Static method which converts {@link IPAddressSeqRange} to list of canonical ip address names
     * separated by spaces, i.e IpAddressSeqRange(10.8.0.2 -> 10.8.0.3) == "10.8.0.2 10.8.0.3"
     *
     * @return converted string or exception, wrapped with {@link Try}
     */
    static Try<String> seqRangeToString(@NonNull IPAddressSeqRange range) {
        return Try.of(() -> range.stream().map(IPAddress::toCanonicalString).collect(Collectors.joining(" ")));
    }

    /**
     * Static method which converts singular ip address (eg. 10.8.0.2) to {@link IPAddressSeqRange}
     * with prefix length of 31, i.e "10.8.0.2" == IpAddressSeqRange(10.8.0.2 -> 10.8.0.3)
     *
     * @return converted range or exception, wrapped with {@link Try}
     */
    static Try<IPAddressSeqRange> seqRangeFromString(String ipString) {
        return Try.of(() -> new IPAddressString(ipString).toAddress())
                .map(address -> address.setPrefixLength(31).toSequentialRange());
    }

}
