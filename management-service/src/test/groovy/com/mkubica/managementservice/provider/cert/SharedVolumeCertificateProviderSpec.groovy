package com.mkubica.managementservice.provider.cert

import spock.lang.*

class SharedVolumeCertificateProviderSpec extends Specification {

    private File caCertFile
    private File taKeyFile
    private CertificateProvider provider

    def setup() {
        caCertFile = File.createTempFile("ca-", ".crt")
        taKeyFile = File.createTempFile("ta-", ".key")
        provider = new SharedVolumeCertificateProvider(caCertFile.getPath(), taKeyFile.getPath())
    }

    def "certificate provider reads certificate from file"() {
        given:
            caCertFile.write("some-cert")
            taKeyFile.write("some-key")

        when:
            def ca = provider.obtainCACert()
            def ta = provider.obtainTAKey()

        then:
            ca.get() == "some-cert\n"
            ta.get() == "some-key\n"
    }

    def "certificate provider fails when doesn't have access to ca.crt file"() {
        given:
            caCertFile.write("some-cert")
            caCertFile.setReadable(false)

        when:
            def ca = provider.obtainCACert()

        then:
            ca.isFailure()
            ca.getCause().getMessage() =~ "Permission denied"
            ca.getCause() instanceof IOException
    }

    def "certificate provider fails when doesn't have access to ta.key file"() {
        given:
            taKeyFile.write("some-key")
            taKeyFile.setReadable(false)

        when:
            def ta = provider.obtainTAKey()

        then:
            ta.isFailure()
            ta.getCause().getMessage() =~ "Permission denied"
            ta.getCause() instanceof IOException
    }

    def "certificate provider fails when ca.crt doesn't exist"() {
        given:
            caCertFile.write("some-cert")
            caCertFile.delete()

        when:
            def ca = provider.obtainCACert()

        then:
            ca.isFailure()
            ca.getCause().getMessage() =~ "No such file or directory"
            ca.getCause() instanceof IOException
    }

    def "certificate provider fails when ta.crt doesn't exist"() {
        given:
            taKeyFile.write("some-key")
            taKeyFile.delete()

        when:
            def ta = provider.obtainTAKey()

        then:
            ta.isFailure()
            ta.getCause().getMessage() =~ "No such file or directory"
            ta.getCause() instanceof IOException
    }


}
