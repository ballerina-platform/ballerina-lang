import ballerina/http;
import ballerina/log;

listener http:Listener helloEP = new(9090);

service /helloWorld on helloEP {
    resource function get sayHello(http:Caller caller, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! ");
        var responseResult = caller->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", err = responseResult);
        }
    }
}
