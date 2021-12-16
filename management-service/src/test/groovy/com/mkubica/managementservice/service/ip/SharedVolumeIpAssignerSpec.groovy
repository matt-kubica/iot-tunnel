package com.mkubica.managementservice.service.ip


import com.mkubica.managementservice.domain.dao.GatewayEntity
import com.mkubica.managementservice.exception.IpAddressNotUniqueException
import com.mkubica.managementservice.exception.IpAddressNotWithinPoolException
import com.mkubica.managementservice.exception.IpAddressPoolFullyAllocatedException
import com.mkubica.managementservice.repository.GatewayRepository
import inet.ipaddr.AddressStringException
import io.vavr.collection.TreeSet
import io.vavr.control.Option
import spock.lang.Specification

import java.nio.file.FileAlreadyExistsException

import static java.lang.String.format


class SharedVolumeIpAssignerSpec extends Specification {

    private final GatewayRepository gatewayRepository = Stub()
    private final String internalAddressPool = "10.8.0.0/24"
    private static File ccDirectory
    private IpAssigner ipAssigner


    def setup() {
        // NOTE: every time new unit test is executed, new temp dir needs to be created
        ccDirectory = File.createTempDir("ccd-")
        ipAssigner = new SharedVolumeIpAssigner(ccDirectory.toString(), gatewayRepository, internalAddressPool)
    }

    def "assign ip explicitly"() {
        given: "repository that doesn't contain any entities"
            gatewayRepository.getGatewayEntityByIpAddress(_ as String) >> Option.none()

        when: "assigning explicit ip to certain common name"
            def res = ipAssigner.assignIp("some-common-name", "10.8.0.14")

        then: "result is successful and carries ip assigned ip address"
            res.isSuccess()
            res.get() == "10.8.0.14"
        and: "corresponding ip seq range is saved to inner cache"
            ipAssigner.allocatedIpPairs.all() == TreeSet.of(
                IpAssigner.seqRangeFromString("10.8.0.14").get()
            )
        and: "file of certain common name has been created and contains corresponding config"
            fileValid("some-common-name", "10.8.0.14 10.8.0.15")
    }

    def "try to assign corrupted ip"() {
        // TODO: add more cases within this using where (more corrupted ips)
        given: "repository that doesn't contain any entities"
            gatewayRepository.getGatewayEntityByIpAddress(_ as String) >> Option.none()

        when: "assigning explicit corrupted ip to any common name"
            def res = ipAssigner.assignIp(_ as String, "10.8.0.329")

        then: "result is failure"
            res.isFailure()
            res.getCause() instanceof AddressStringException
        and: "cache is empty"
            ipAssigner.allocatedIpPairs.all() == TreeSet.empty()
        and: "no config file has been created"
            ccDirectory.listFiles().size() == 0
    }

    def "try to assign ip that is not within pool"() {
        given: "repository that doesn't contain any entities"
            gatewayRepository.getGatewayEntityByIpAddress(_ as String) >> Option.none()

        when: "assigning explicit ip from outside a internal address pool to any common name"
            def res = ipAssigner.assignIp(_ as String, "10.7.0.14")

        then: "result is failure"
            res.isFailure()
            res.getCause() instanceof IpAddressNotWithinPoolException
        and: "cache is empty"
            ipAssigner.allocatedIpPairs.all() == TreeSet.empty()
        and: "no config file has been created"
            ccDirectory.listFiles().size() == 0
    }

    def "try to assign ip that has been already assigned"() {
        given: "repository that contains any entity associated with certain ip"
            gatewayRepository.getGatewayEntityByIpAddress("10.8.0.14") >> Option.some(GatewayEntity.builder().build())

        when: "assigning same ip to any common name"
            def res = ipAssigner.assignIp(_ as String, "10.8.0.14")

        then: "result is failure"
            res.isFailure()
            res.getCause() instanceof IpAddressNotUniqueException
        and: "cache is empty"
            ipAssigner.allocatedIpPairs.all() == TreeSet.empty()
        and: "no config file has been created"
            ccDirectory.listFiles().size() == 0
    }

    def "try to assign ip to orphaned common name (associated common name still exist in ccd)"() {
        given: "repository that doesn't contain any entities"
            gatewayRepository.getGatewayEntityByIpAddress(_ as String) >> Option.none()
        and: "ccd containing orphaned config"
            new File(ccDirectory, "some-common-name").createNewFile()

        when: "assigning ip to common name that have associated orphaned config"
            def res = ipAssigner.assignIp("some-common-name", "10.8.0.14")

        then: "result is failure"
            res.isFailure()
            res.getCause() instanceof FileAlreadyExistsException
        and: "cache is empty"
            ipAssigner.allocatedIpPairs.all() == TreeSet.empty()
        and: "no new config file has been created"
            ccDirectory.listFiles().size() == 1
    }

    def "allocate ip address automatically"() {
        when: "assigning ip address by providing only common name (cache and ccd are empty)"
            def res = ipAssigner.assignIp("some-common-name")

        then: "result is successful and carries assigned ip address"
            res.isSuccess()
            res.get() == "10.8.0.2"
        and: "cache contains successfully assigned ip"
            ipAssigner.allocatedIpPairs.all() == TreeSet.of(IpAssigner.seqRangeFromString("10.8.0.2").get())
        and: "associated config file has been created"
            fileValid("some-common-name", "10.8.0.2 10.8.0.3")


    }

    def "allocate ip address automatically when some ip addresses have been already assigned"() {
        // TODO: this should have more cases of how ips are assigned
        when: "couple of ips have been assigned automatically"
            ipAssigner.assignIp("some-common-name-1")
            ipAssigner.assignIp("some-common-name-2")
            def res = ipAssigner.assignIp("some-common-name-3")

        then: "result is successful and carries assigned ip address"
            res.isSuccess()
            res.get() == "10.8.0.6"
        and: "associated config files have been created"
            fileValid("some-common-name-1", "10.8.0.2 10.8.0.3")
            fileValid("some-common-name-2", "10.8.0.4 10.8.0.5")
            fileValid("some-common-name-3", "10.8.0.6 10.8.0.7")
        and: "cache contains successfully assigned addresses"
            ipAssigner.allocatedIpPairs.all() == TreeSet.of(
                    IpAssigner.seqRangeFromString("10.8.0.2").get(),
                    IpAssigner.seqRangeFromString("10.8.0.4").get(),
                    IpAssigner.seqRangeFromString("10.8.0.6").get()
            )
    }

    def "try to allocate more ip addresses than possible"() {
        given: "number of addresses which equals to maximum capacity of address pool"
            def amount = IpAssigner.getAllPairsFromCidr("10.8.0.0/24").get().size() - 1
            amount.times { ipAssigner.assignIp("cn-$it").get() }

        when: "assigning one more address automatically"
            def res = ipAssigner.assignIp("other")

        then: "result is failure"
            res.isFailure()
            res.getCause() instanceof IpAddressPoolFullyAllocatedException
        and: "cache is full"
            ipAssigner.allocatedIpPairs.all().size() == amount
        and: "ccd is full"
            ccDirectory.listFiles().size() == amount
    }

    def "revoke ip address"() {
        given: "empty repository"
            gatewayRepository.getGatewayEntityByIpAddress(_ as String) >> Option.none()
            gatewayRepository.getGatewayEntityByCommonName("some-common-name-2")
                    >> Option.some(GatewayEntity.builder().withIpAddress("10.8.0.24").build())

        when: "assigning couple of ip addresses"
            ipAssigner.assignIp("some-common-name-1", "10.8.0.2")
            ipAssigner.assignIp("some-common-name-2", "10.8.0.24")
            ipAssigner.assignIp("some-common-name-3", "10.8.0.12")
        and: "revoking one of them"
            def res = ipAssigner.revokeIp("some-common-name-2")

        then: "result is successful"
            res.isSuccess()
            res.get() == "10.8.0.24"
        and: "cache contains only addresses that haven't been revoked"
            ipAssigner.allocatedIpPairs.all() == TreeSet.of(
                    IpAssigner.seqRangeFromString("10.8.0.2").get(),
                    IpAssigner.seqRangeFromString("10.8.0.12").get(),
            )
        and: "ccd contains only configs associated with common names that havent been unassigned from ip"
            fileValid("some-common-name-1", "10.8.0.2 10.8.0.3")
            fileValid("some-common-name-3", "10.8.0.12 10.8.0.13")
    }

    def "try to revoke ip from CN with no associated config file"() {
        given: "repository that doesn't contain any entities"
            gatewayRepository.getGatewayEntityByIpAddress(_ as String) >> Option.none()

        when: "assigning couple of ip addresses"
            ipAssigner.assignIp("some-common-name-1", "10.8.0.2")
            ipAssigner.assignIp("some-common-name-2", "10.8.0.24")
            ipAssigner.assignIp("some-common-name-3", "10.8.0.12")
        and: "removing config of certain common name"
            new File(ccDirectory, "some-common-name-2").delete()
        and: "revoking ip of this certain common name"
            def res = ipAssigner.revokeIp("some-common-name-2")

        then: "result is failure"
            res.isFailure()
            res.getCause() instanceof FileNotFoundException
        and: "cache contains all assigned ip addresses"
            ipAssigner.allocatedIpPairs.all() == TreeSet.of(
                    IpAssigner.seqRangeFromString("10.8.0.2").get(),
                    IpAssigner.seqRangeFromString("10.8.0.12").get(),
                    IpAssigner.seqRangeFromString("10.8.0.24").get(),
            )
        and: "ccd contains all config files despite one that have been deleted on purpose"
            fileValid("some-common-name-1", "10.8.0.2 10.8.0.3")
            fileValid("some-common-name-3", "10.8.0.12 10.8.0.13")
    }

    def "try to initialize ip assigner when repository holds corrupted ip addresses"() {
        given: "fresh stub repository"
            def repository = Stub(GatewayRepository)
        and: "some entities in this repository, one of them has corrupted ip"
            repository.findAll() >> List.of(
                    GatewayEntity.builder().withIpAddress("10.8.0.14").build(),
                    GatewayEntity.builder().withIpAddress("10.8.0.24").build(),
                    GatewayEntity.builder().withIpAddress("invalid").build()
            )

        expect: "creation of ip assigner to succeed"
            def assigner = new SharedVolumeIpAssigner(ccDirectory.toString(), repository, "10.8.0.0/24")
        and: "cache to not contain corrupted ip"
            assigner.allocatedIpPairs.all() == TreeSet.of(
                    IpAssigner.seqRangeFromString("10.8.0.14").get(),
                    IpAssigner.seqRangeFromString("10.8.0.24").get()
            )
    }

    static boolean fileValid(String commonName, String contents) {
        def file = new File(ccDirectory, commonName)

        file.exists() && ccDirectory.listFiles().contains(file) && \
            new FileReader(file).readLine() == format("ifconfig-push %s", contents)
    }
}
