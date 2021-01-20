import ballerina/http;
import ballerina/log;
import ballerina/cloud as _;

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

    resource function get readyz(http:Caller caller, http:Request req) {
        json j = {
            message:"hello"
        };
        http:Response res = new;
        res.setJsonPayload(j);
        //Reply to the client with the response.
        var result = caller->respond(res);
        if (result is error) {
           log:printError("Error in responding", err = result);
        }
    }
}
