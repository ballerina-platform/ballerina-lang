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



@transactions:Participant {
    oncommitFunc: baz,
    onabortFunc: bar
}
function foo(int i) returns int {
    return i;
}

function baz(string id) {

}

function bar(string id) {

}
