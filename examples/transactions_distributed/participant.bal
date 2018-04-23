import ballerina/log;
import ballerina/io;
import ballerina/http;

// This service is a participant in the distributed transaction. It will get infected when it receives a transaction
// context from the participant. The transaction context, in the HTTP case, will be passed in as custom HTTP headers.

endpoint http:Listener participantEP {
    port:8889
};

@http:ServiceConfig {
    basePath:"/stockquote"
}
service ParticipantService bind participantEP {

    @http:ResourceConfig {
        path:"/update"
    }
    updateStockQuote(endpoint conn, http:Request req) {
        log:printInfo("Received update stockquote request");
        http:Response res = new;

        // At the beginning of the transaction statement, since a transaction context has been received, this service
        // will register with the initiator as a participant.
        transaction {
            var updateReq = untaint req.getJsonPayload();
            match updateReq{
                json updateReqJson => {
                    string msg = io:sprintf("Update stock quote request received. symbol:%j, price:%j",
                        updateReqJson.symbol, updateReqJson.price);
                    log:printInfo(msg);

                    json jsonRes = {"message":"updating stock"};
                    res.statusCode = http:OK_200;
                    res.setJsonPayload(jsonRes);
                }
                http:PayloadError payloadError => {
                    res.statusCode = http:INTERNAL_SERVER_ERROR_500;
                    res.setStringPayload(payloadError.message);
                    log:printError("Payload error occurred!", err = payloadError);
                }
            }

            var result = conn->respond(res);
            match result {
                error e => log:printError("Could not send response back to initiator", err = e);
                () => log:printInfo("Sent response back to initiator");
            }
        }
    }
}
