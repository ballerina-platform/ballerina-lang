import ballerina/http;

endpoint http:Listener listener {
    port:9600
};

channel<json> chn;

@interruptible
service<http:Service> channelService bind listener {

    receiveChannelMessage (endpoint caller, http:Request request) {

        http:Response response = new;
        json result;
        map key = { line1: "No. 20", line2: "Palm Grove", city: "Colombo 03", country: "Sri Lanka" };
        result <- chn,key;
        response.setJsonPayload(result, contentType = "application/json");
        _ = caller -> respond(response);
    }


    sendChannelMessage (endpoint caller, http:Request request) {

        // Create object to carry data back to caller
        http:Response response = new;

        json result = {message:"channel_message"};
        map key = { line1: "No. 20", line2: "Palm Grove", city: "Colombo 03", country: "Sri Lanka" };
        result -> chn,key;

        response.setJsonPayload(result, contentType = "application/json");
        _ = caller -> respond(response);
    }
}
