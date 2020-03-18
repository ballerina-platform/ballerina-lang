import ballerina/http;
import ballerina/log;

service helloModule on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req) {
        var result = caller->respond("Hello, Module!");
        if (result is error) {
            log:printError("Error sending response", result);
        }
    }
}

