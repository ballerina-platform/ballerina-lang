import ballerina/http;
import ballerina/log;
import ballerina/math;
import ballerina/transactions;

// This is the initiator of the distributed transaction.
@http:ServiceConfig {
    basePath: "/"
}
service InitiatorService on new http:Listener(8080) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function init(http:Caller conn, http:Request req) {
        http:Response res = new;
        log:printInfo("Initiating transaction...");
        // When the `transaction` statement starts, a distributed transaction context is created.
        transaction {
            // Print the current transaction ID
            log:printInfo("Started transaction: " +
                          transactions:getCurrentTransactionId());
            // When a participant is called, the transaction context is propagated, and that participant
            // gets infected and joins the distributed transaction.
            boolean successful = callBusinessService();
            if (successful) {
                res.statusCode = http:STATUS_OK;
            } else {
                res.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
                abort;
            }
        // As soon as the `transaction` block ends, the `2-phase commit
        // coordination` protocol will run. All participants are prepared
        // and depending on the joint outcome, either a `notify commit` or
        // `notify abort` will be sent to the participants.
        } committed {
            log:printInfo("Initiated transaction committed");
        } aborted {
            log:printInfo("Initiated transaction aborted");
        }
        // Send the response back to the client.
        var result = conn->respond(res);
        if (result is error) {
            log:printError("Could not send response back to client",
                            err = result);
        } else {
            log:printInfo("Sent response back to client");
        }
    }
}

// This is the participant business function call.
function callBusinessService() returns boolean {
    http:Client participantEP = new ("http://localhost:8889/stockquote/update");
    // Generate the payload
    float price = <int>math:randomInRange(200, 250) + math:random();
    json bizReq = {symbol: "GOOG", price: price};
    // Send the request to the backend service.
    http:Request req = new;
    req.setJsonPayload(bizReq);
    var result = participantEP->post("", req);
    log:printInfo("Got response from bizservice");
    if (result is error) {
        log:printError("Error when calling the backend: ", err = result);
        return false;
    } else {
        return result.statusCode == http:STATUS_OK;
    }
}
