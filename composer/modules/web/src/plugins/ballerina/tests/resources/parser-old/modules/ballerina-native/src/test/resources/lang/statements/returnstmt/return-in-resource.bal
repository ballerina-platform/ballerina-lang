import ballerina/http;

service<http> helloWorld {

    resource sayHello(message m) {
        message response = {};
        return response;
    }
}
