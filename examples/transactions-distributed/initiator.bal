import ballerina/http;
import ballerina/io;
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
        io:println("Initiating transaction...");

        // When the `transaction` statement starts, a distributed transaction context is created.
        transaction {

            // Print the current transaction ID
            io:println("Started transaction: " + transactions:getCurrentTransactionId());

            // When a participant is called, the transaction context is propagated, and that participant
            // gets infected and joins the distributed transaction.
            boolean successful = callBusinessService();
            if (successful) {
                res.statusCode = http:OK_200;
            } else {
                res.statusCode = http:INTERNAL_SERVER_ERROR_500;
                abort;
            }

            // As soon as the `transaction` block ends, the `2-phase commit
            // coordination` protocol will run. All participants are prepared
            // and depending on the joint outcome, either a `notify commit` or
            // `notify abort` will be sent to the participants.
        } committed {
            io:println("Initiated transaction committed");
        } aborted {
            io:println("Initiated transaction aborted");
        }

        var result = conn->respond(res);
        if (result is error) {
            io:println("Could not send response back to client");
        } else {
            io:println("Sent response back to client");
        }
    }
}

function callBusinessService() returns boolean {
    http:Client participantEP = new("http://localhost:8889/stockquote/update");

    float price = math:randomInRange(200, 250) + math:random();
    json bizReq = { symbol: "GOOG", price: price };
    http:Request req = new;
    req.setJsonPayload(bizReq);
    var result = participantEP->post("", req);
    io:println("Got response from bizservice");
    if (result is error) {
        return false;
    }  else {
        return (result.statusCode == http:OK_200);
    }
}
