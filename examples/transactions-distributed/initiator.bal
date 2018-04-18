import ballerina/math;
import ballerina/http;
import ballerina/log;

// This is the initiator of the distributed transaction.
endpoint http:ServiceEndpoint initiatorEP {
    port:8080
};
@http:ServiceConfig {
    basePath:"/"
}
service<http:Service> InitiatorService bind initiatorEP{

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    init (http:Connection conn, http:Request req) {
        http:Response res = new;
        log:printInfo("Initiating transaction...");

        // When the transaction statement starts, a distributed transaction context is created.
        transaction {
            // When a participant is called, the transaction context is propagated, and that participant
            // gets infected and joins the distributed transaction.
            boolean successful = callBusinessService();
            if (successful) {
                res = {statusCode:200};
            } else {
                res = {statusCode:500};
            }
        }
        // As soon as the transaction block ends, the `2-phase commit coordination` protocol will run. All participants
        // are prepared and depending on the join outcome, either a `notify commit` or `notify abort` will
        // be sent to the participants.

        var err = conn.respond(res);
        if (err != null) {
            log:printErrorCause("Could not send response back to client", err);
        } else {
            log:printInfo("Sent response back to client");
        }
    }
}

function callBusinessService () returns (boolean) {
    endpoint<BizClient> participantEP {
        create BizClient();
    }

    boolean successful;

    float price = math:randomInRange(200, 250) + math:random();
    json bizReq = {symbol:"GOOG", price:price};
    var _, e = participantEP.updateStock(bizReq, "127.0.0.1", 8889);
    if (e != null) {
        successful = false;
    } else {
        successful = true;
    }
    return successful;
}

public connector BizClient () {

    action updateStock (json bizReq, string host, int port) returns (json jsonRes, error err) {
        endpoint<http:HttpClient> bizEP {
            create http:HttpClient("http://" + host + ":" + port + "/stockquote/update", {});
        }
        http:Request req = new;
        req.setJsonPayload(bizReq);
        var res, e = bizEP.post("", req);
        log:printInfo("Got response from bizservice");
        if (e == null) {
            if (res.statusCode != 200) {
                err = {message:"Error occurred"};
            } else {
                jsonRes, payloadError = res.getJsonPayload();
                if (payloadError != null) {
                    err = (error)payloadError;
                }
            }
        } else {
            err = (error)e;
        }
        return;
    }
}
