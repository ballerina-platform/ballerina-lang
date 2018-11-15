import ballerina/http;
import ballerina/log;
import ballerina/transactions;

@http:ServiceConfig {
    basePath: "/hello"
}
service<http:Service> hello bind { port: 9090 } {

    @transactions:Participant {
        oncommitFunc: baz,
        onabortFunc: bar,
        canInitiate: true
    }
    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;

        res.setPayload("Hello, World!");

        caller->respond(res) but { error e => log:printError(
                                                  "Error sending response", err = e) };
    }
}

string S1 = "";

function baz(string id) {
    S1 = S1 + " in baz[oncommittedFunc]";
}

function bar(string id) {
    S1 = S1 + " in bar[onabortFunc]";
}