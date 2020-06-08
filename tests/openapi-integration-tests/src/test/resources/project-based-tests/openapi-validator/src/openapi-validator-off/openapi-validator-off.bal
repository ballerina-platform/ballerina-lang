import ballerina/http;
import ballerina/log;
import ballerina/openapi;

        listener http:Listener ep0 = new(9091, config = {host: "localhost"});

@openapi:ServiceInfo {
        contract: "src/openapi-validator-off/resources/openapi_validator_off.yaml",
        failOnErrors: false
}
@http:ServiceConfig {
        basePath: "/api/v1"
}
service openapi_validator_off on ep0{
    @http:ResourceConfig {
        methods:["GET"],
        path:"/{param1}/{param3}"
    }
    resource function test2Params (http:Caller caller, http:Request req,  string param1,  string param3) returns error? {
        string msg = "Hello, " + param1 + " " + param3 ;
        var result = caller->respond(<@untained> msg);
        if (result is error) {
            log:printError("Error sending response", result);
        }
    }
}
