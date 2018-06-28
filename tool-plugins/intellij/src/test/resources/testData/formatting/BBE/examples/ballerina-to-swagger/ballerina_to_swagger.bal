import ballerina/http;
import ballerina/log;

endpoint http:Listener helloEp {
    port: 9090
};

service hello bind helloEp {
    hi(endpoint caller, http:Request request) {
        http:Response res;
        res.setPayload("Hello World!");
        caller->respond(res) but {
            error e => log:printError("Error when responding", err = e)
        };
    }
}
