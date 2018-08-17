import ballerina/http;

service<http:Service> hello bind { port: 9090 } {
    sayHello(endpoint caller, http:Request req) {
        /*ref*/fun();
    }
}

function /*def*/fun () {

}
