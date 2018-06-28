import ballerina/math;
import ballerina/http;
import ballerina/log;
import ballerina/transactions;

// This is the initiator of the distributed transaction.
@http:ServiceConfig {
    basePath: "/"
}
service<http:Service> InitiatorService bind { port: 8080 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    init(endpoint conn, http:Request req) {
        http:Response res = new;
        log:printInfo("Initiating transaction...");

        // When the transaction statement starts, a distributed transaction context is created.
        transaction with oncommit = printCommit,
        onabort = printAbort {

        // Print the current transaction ID
            log:printInfo("Started transaction: " +
                    transactions:getCurrentTransactionId());

            // When a participant is called, the transaction context is propagated, and that participant
            // gets infected and joins the distributed transaction.
            boolean successful = callBusinessService();
            if (successful) {
                res.statusCode = http:OK_200;
            } else {
                res.statusCode = http:INTERNAL_SERVER_ERROR_500;
                abort;
            }

        // As soon as the transaction block ends, the `2-phase commit
        // coordination` protocol will run. All participants are prepared
        // and depending on the joint outcome, either a `notify commit` or
        // `notify abort` will be sent to the participants.
        }

        var result = conn->respond(res);
        match result {
            error e =>
            log:printError("Could not send response back to client", err = e);
            () =>
            log:printInfo("Sent response back to client");
        }
    }
}

// The initiator function that will get called when the distributed transaction
// is aborted
function printAbort(string transactionId) {
    log:printInfo("Initiated transaction: " + transactionId + " aborted");
}

// The initiator function that will get called when the distributed transaction
// is committed
function printCommit(string transactionId) {
    log:printInfo("Initiated transaction: " + transactionId + " committed");
}

function callBusinessService() returns boolean {
    endpoint http:Client participantEP {
        url: "http://localhost:8889/stockquote/update"
    };

    boolean successful;

    float price = math:randomInRange(200, 250) + math:random();
    json bizReq = { symbol: "GOOG", price: price };
    http:Request req = new;
    req.setJsonPayload(bizReq);
    var result = participantEP->post("", req);
    log:printInfo("Got response from bizservice");
    match result {
        http:Response res => {
            successful = (res.statusCode == http:OK_200) ? true : false;
        }
        error => successful = false;
    }
    return successful;
}
