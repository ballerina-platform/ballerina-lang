import ballerina/http;

service helloService on new http:Listener(8080) {
    int testVal = 12;
    resource function sayHello(http:Caller caller, http:Request request) {
        helloService.
    }

    function serviceFunc() {
    }
}
