import ballerina.io;
import ballerina.net.http;

function main (string[] args) {
    endpoint<http:HttpClient> httpEndoint {
        create http:HttpClient("http://www.mocky.io", getConnectorConfigs());
    }
    http:Request req = {};

    //Send a GET request to the specified endpoint
    http:Response resp;
    resp, _ = httpEndoint.get("/v2/59d590762700000a049cd694", req);
    var payload, payloadError = resp.getStringPayload();
    if (payloadError == null) {
        io:println("Response received for the GET request is : " + payload);
    } else {
        io:println("Error occurred : " + payloadError.message);
    }

}

function getConnectorConfigs () (http:Options) {
    //Enable auto redirects and give a maximum redirect count.
    http:Options option = {ssl:{}, followRedirects:{}};
    option.followRedirects.enabled = true;
    option.followRedirects.maxCount = 5;
    return option;
}
