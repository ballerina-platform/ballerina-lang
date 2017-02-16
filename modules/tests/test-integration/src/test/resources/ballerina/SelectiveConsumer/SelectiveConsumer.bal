package bal.integration.selectiveConsumer;

import ballerina.net.http;
import ballerina.lang.message;
import ballerina.lang.json;
import ballerina.lang.string;

@BasePath("/selectiveConsumer")
service SelectiveConsumerService{

    @POST
    @Path ("/invoke")
    resource selectiveConsumerResource (message m) {
        message response;

        response = requestSender(m);
        reply response;
    }
}

function validateRequest(message msg)(boolean validationStatus){
    json payload;
    string crediability;

    payload = message:getJsonPayload(msg);
    crediability = json:getString(payload, "$.creditRequest.checkStatus");
    if(string:equalsIgnoreCase(crediability, "special")){
        validationStatus = true;
    }
    else{
        validationStatus = false;
    }
    return;
}

function requestSender(message incomingMsg)(message response){
    http:HTTPConnector ep = new http:HTTPConnector ("http://localhost:9090");

    boolean status;

    status = validateRequest(incomingMsg);
    if(status){
        response = http:HTTPConnector.post(ep, "/bankCreditService/specialityCreditDep", incomingMsg);
    }
    else{

    }
    return;

}
