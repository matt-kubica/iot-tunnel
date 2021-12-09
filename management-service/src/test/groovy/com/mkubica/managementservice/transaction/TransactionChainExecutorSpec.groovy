package com.mkubica.managementservice.transaction

import io.vavr.control.Try
import spock.lang.Specification

import static com.mkubica.managementservice.transaction.Transaction.pure

class TransactionChainExecutorSpec extends Specification {

    private final TransactionChainExecutor executor = new TransactionChainExecutor()

    def "pure transactions"() {
        given:
            def assignerService = new FakeAssignerService()
            def transactionChain = TransactionChain.of(
                    pure(m -> assignerService.assignSuccessfully(m + "a" as String)),
                    pure(m -> assignerService.assignSuccessfully(m + "b" as String)),
                    pure(m -> assignerService.assignSuccessfully(m + "c" as String)),
            )

        when:
            def res = executor.executeChain("", transactionChain)

        then:
            res.isSuccess()
            res.get() == "abc"

            transactionChain.getSuccessfulTransactionsWithStates().size() == 3
            transactionChain.getDefinedTransactions().size() == 3
            assignerService.getState() == ["a", "ab", "abc"]
    }


    private static class FakeAssignerService {

        List<String> state = new ArrayList<>()

        Try<String> assignSuccessfully(String param) {
            state.add(param)
            return Try.success(param)
        }

        Try<Void> revokeSuccessfully(String param) {
            state.remove(param)
            return Try.success(null)
        }

        Try<String> assignIneffectively(String param) {
            return Try.failure(new RuntimeException())
        }

        Try<Void> revokeIneffectively(String param) {
            return Try.failure(new RuntimeException())
        }
    }
}
