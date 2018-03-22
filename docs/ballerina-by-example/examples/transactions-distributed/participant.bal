import ballerina/log;
import ballerina/io;
import ballerina/net.http;
import ballerina/mime;
// This service is a participant in the distributed transaction. It will get infected when it receives a transaction
// context from the participant. The transaction context, in the HTTP case, will be passed in as custom HTTP headers.

endpoint http:ServiceEndpoint participantEp {
    port:9090,
    host:"localhost"
};
@http:ServiceConfig {endpoints:[participantEp], basePath:"stockquote"}

service<http:Service> ParticipantService {

    @http:ResourceConfig {
        path:"/update"
    }
    updateStockQuote (endpoint conn, http:Request req) {
        log:printInfo("Received update stockquote request");
        http:Response res = {};

        // At the beginning of the transaction statement, since a transaction context has been received, this service
        // will register with the initiator as a participant.
        transaction {
            var request = req.getJsonPayload();
            match request {
                json updateReq => {
                    string msg = io:sprintf("Update stock quote request received. symbol:%j, price:%j",
                                            [updateReq.symbol, updateReq.price]);
                    log:printInfo(msg);

                    json jsonRes = {"message":"updating stock"};
                    res = {statusCode:200};
                    res.setJsonPayload(jsonRes);
                }
                mime:EntityError payloadError => {
                    res = {statusCode:500};
                    res.setStringPayload(payloadError.message);
                    log:printErrorCause("Payload error occurred!", payloadError.cause[0]);
                }
            }

            http:HttpConnectorError err = conn -> respond(res);
            if (err != null) {
                log:printErrorCause("Could not send response back to initiator", err.cause[0]);
            } else {
                log:printInfo("Sent response back to initiator");
            }
        }
    }
}
