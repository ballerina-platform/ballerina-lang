import ballerina.log;
import ballerina.io;
import ballerina.net.http;

// This service is a participant in the distributed transaction. It will get infected when it receives a transaction
// context from the participant. The transaction context, in the HTTP case, will be passed in as custom HTTP headers.
@http:configuration {
    basePath:"/stockquote",
    host:"localhost",
    port:8889
}
service<http> ParticipantService {

    @http:resourceConfig {
        path:"/update"
    }
    resource updateStockQuote (http:Connection conn, http:InRequest req) {
        log:printInfo("Received update stockquote request");
        http:OutResponse res;

        // At the beginning of the transaction statement, since a transaction context has been received, this service
        // will register with the initiator as a participant.
        transaction {
            json updateReq = req.getJsonPayload();
            string msg = io:sprintf("Update stock quote request received. symbol:%j, price:%j",
                                    [updateReq.symbol, updateReq.price]);
            log:printInfo(msg);

            json jsonRes = {"message":"updating stock"};
            res = {statusCode:200};
            res.setJsonPayload(jsonRes);
            var err = conn.respond(res);
            if (err != null) {
                log:printErrorCause("Could not send response back to initiator", err);
            } else {
                log:printInfo("Sent response back to initiator");
            }
        }
    }
}
