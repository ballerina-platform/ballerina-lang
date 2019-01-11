import ballerina/http;
import ballerina/log;

listener http:Listener helloEp = new(9090);

service hello on helloEp {
    resource function hi(http:Caller caller, http:Request request) {
        http:Response res = new;
        res.setPayload("Hello World!");

        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error when responding", err = result);
        }
    }
}