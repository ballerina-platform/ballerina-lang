import ballerina/http;

public function inTransactionLocalEndpoints() {
     transaction with retries = 0 {
         http:Client clientEPInTransactionBody = new("");
         var resp = clientEPInTransactionBody->get("");
     } onretry {
        http:Client clientEPInRetryBody = new("");
        var resp = clientEPInRetryBody->get("");
     } committed {
        http:Client clientEPInCommittedBody = new("");
        var resp = clientEPInCommittedBody->get("");
     } aborted {
        http:Client clientEPInAbortedBody = new("");
        var resp = clientEPInAbortedBody->get("");
     }
}