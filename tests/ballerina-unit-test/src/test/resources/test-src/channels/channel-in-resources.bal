import ballerina/http;

endpoint http:Listener listener {
    port:9397
};

channel<json> chn;

service<http:Service> hello bind listener {
    receive(endpoint caller, http:Request request) {

        http:Response response = new;

        json result = {};
        map key = { line1: "No. 20", line2: "Palm Grove", city: "Colombo 03", country: "Sri Lanka" };
        result -> chn,key;
        result <- chn, key;
        // Objects and structs can have function calls
        response.setJsonPayload(result, contentType = "application/json");

        _ = caller->respond(response);
    }

}
