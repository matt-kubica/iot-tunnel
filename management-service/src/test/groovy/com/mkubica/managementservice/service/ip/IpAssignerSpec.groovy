package com.mkubica.managementservice.service.ip

import inet.ipaddr.AddressStringException
import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressSeqRange
import inet.ipaddr.IPAddressString
import inet.ipaddr.ipv4.IPv4AddressSeqRange
import spock.lang.Specification

import java.util.stream.Collectors

import static java.lang.String.format


class IpAssignerSpec extends Specification {

    def "get all ip pairs from cidr string"() {
        given:
            def internalAddressPool = "10.8.0.0/24"
            def initialPoolSize = new IPAddressString(internalAddressPool).toAddress()
                    .stream().collect(Collectors.toList()).size()

        when:
            def res = IpAssigner.getAllPairsFromCidr(internalAddressPool)

        then:
            res.isSuccess()
            res.get().size() == (initialPoolSize / 2) as int
            res.get().each { it.count as int == 2 }
    }

    def "try to get all ip pairs from invalid cidr string"() {
        given:
            def internalAddressPool = "10.8.268.0/24"

        when:
            def res = IpAssigner.getAllPairsFromCidr(internalAddressPool)

        then:
            res.isFailure()
            res.getCause() instanceof AddressStringException
    }

    def "get all ip pairs from network address and mask strings"() {
        given:
            def internalNetworkAddress = "10.8.0.0"
            def internalNetworkMask = "255.255.255.0"
            def initialPoolSize = new IPAddressString(format("%s/%s", internalNetworkAddress, internalNetworkMask)).toAddress()
                .stream().collect(Collectors.toList()).size()

        when:
            def res = IpAssigner.getAllPairsFromAddressAndMask(internalNetworkAddress, internalNetworkMask)

        then:
            res.isSuccess()
            res.get().size() == (initialPoolSize / 2) as int
            res.get().each { it.count as int == 2 }
    }

    def "get all ip pairs from invalid network address and mask strings"() {
        given:
            def internalNetworkAddress = "10.8.312.0"
            def internalNetworkMask = "255.243"

        when:
            def res = IpAssigner.getAllPairsFromAddressAndMask(internalNetworkAddress, internalNetworkMask)

        then:
            res.isFailure()
            res.getCause() instanceof AddressStringException
    }

    def "convert IPAddressSeqRange to whitespace divided list of ipv4 addresses"() {
        given:
            def seqRange = from("10.8.0.0", "10.8.0.1")

        when:
            def res = IpAssigner.seqRangeToString(seqRange)

        then:
            res.isSuccess()
            res.get() == "10.8.0.0 10.8.0.1"
    }

    def "convert ip address string to IPAddressSeqRange with 31 prefix length"() {
        given:
            def ipString = "10.8.0.4"

        when:
            def res = IpAssigner.seqRangeFromString(ipString)

        then:
            res.isSuccess()
            res.get() == from("10.8.0.4", "10.8.0.5")
    }

    private static IPAddressSeqRange from(String first, String second) {
        return new IPv4AddressSeqRange(
                new IPAddressString(first).toAddress(IPAddress.IPVersion.IPV4),
                new IPAddressString(second).toAddress(IPAddress.IPVersion.IPV4)
        )
    }
}
