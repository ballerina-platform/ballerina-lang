import ballerina/http;
import ballerina/log;

endpoint http:Listener helloEp {
    port: 9090
};

service hello bind helloEp {
    hi(endpoint caller, http:Request request) {
        http:Response res;
        res.setPayload("Hello World!");
        var result = caller->respond(res);
        if (result is error) {
           log:printError("Error when responding", err = result);
        }
    }
}
