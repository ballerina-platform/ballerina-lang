import ballerina/http;
import ballerina/log;
import ballerina/cloud as _;

listener http:Listener helloEP = new(9090);

configurable string name = ?;

service /helloWorld on helloEP {
    resource function get sayHello(http:Caller caller, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! " + name);
        var responseResult = caller->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", err = responseResult);
        }
    }
}
