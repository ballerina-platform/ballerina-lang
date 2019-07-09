import ballerina/http;

channel<json> chn = new;

service hello on new http:MockListener(9397) {
    resource function receive(http:Caller caller, http:Request request) {

        http:Response response = new;

        json result = {};
        map<any> key = { line1: "No. 20", line2: "Palm Grove", city: "Colombo 03", country: "Sri Lanka" };
        result -> chn,key;
        result = <- chn, key;
        // Objects and structs can have function calls
        response.setJsonPayload(result, contentType = "application/json");

        checkpanic caller->respond(response);
    }

}
