import ballerina/http;
import ballerina/log;
import ballerina/transactions;


//@http:ServiceConfig {
//    basePath: "/hello"
//}
//service<http:Service> hello bind { port: 9090 } {
//
//    sayHello(endpoint caller, http:Request req) {
//        http:Response res = new;
//
//        res.setPayload("Hello, World!");
//
//        caller->respond(res) but { error e => log:printError(
//                                                  "Error sending response", err = e) };
//    }
//}


string S1 = "";

@transactions:Participant {
    oncommitFunc: baz,
    onabortFunc: bar
}
function foo(int i) returns int {
    S1 = S1 + " foo";
    return i;
}

function baz(string id) {
    S1 = S1 + " in baz[oncommittedFunc]";
}

function bar(string id) {
    S1 = S1 + " in bar[onabortFunc]";
}

function initiatorFunc() returns string {
    transaction with retries=2 {
        S1 = S1 + "before";
        int i = foo(5);
        S1 = S1 + " returned:" + i;
    } onretry {
        S1 = S1 + " onretry";
    } committed {
        S1 = S1 + " committed";
    } aborted {
        S1 = S1 + " aborted";
    }
    S1 = S1 + " after trx";
    return S1;
}
