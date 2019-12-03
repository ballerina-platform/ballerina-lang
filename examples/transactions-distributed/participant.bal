import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/transactions;

// This service is a participant in the distributed transaction. It will get
// infected when it receives a transaction context from the participant. The
// transaction context, in the HTTP case, will be passed in as custom HTTP headers.
@http:ServiceConfig {
    basePath: "/stockquote"
}
service ParticipantService on new http:Listener(8889) {

    @http:ResourceConfig {
        path: "/update"
    }
    @transactions:Participant {
        oncommit:printParticipantCommit,
        onabort:printParticipantAbort
    }
    resource function updateStockQuote(http:Caller conn, http:Request req) {
        log:printInfo("Received update stockquote request");
        http:Response res = new;

        // At the beginning of the `transaction` statement, since a transaction
        // context has been received, this service will register with the
        // initiator as a participant.

        // Print the current transaction ID
        log:printInfo("Joined transaction: " +
                       transactions:getCurrentTransactionId());

        var updateReq = <@untainted> req.getJsonPayload();
        if (updateReq is json) {
            string msg =
                io:sprintf("Update stock quote request received. " +
                            "symbol:%s, price:%s",
                            updateReq.symbol, updateReq.price);
            log:printInfo(msg);

            json jsonRes = { "message": "updating stock" };
            res.statusCode = http:STATUS_OK;
            res.setJsonPayload(jsonRes);
        } else {
            res.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
            res.setPayload(updateReq.reason());
            log:printError("Payload error occurred!", err = updateReq);
        }

        var result = conn->respond(res);
        if (result is error) {
            log:printError("Could not send response back to initiator",
                                 err = result);
        } else {
            log:printInfo("Sent response back to initiator");
        }
    }
}

// The participant function that will get called when the distributed
// transaction is aborted
function printParticipantAbort(string transactionId) {
    log:printInfo("Participated transaction: " + transactionId + " aborted");
}

// The participant function that will get called when the distributed
// transaction is committed
function printParticipantCommit(string transactionId) {
    log:printInfo("Participated transaction: " + transactionId + " committed");
}
