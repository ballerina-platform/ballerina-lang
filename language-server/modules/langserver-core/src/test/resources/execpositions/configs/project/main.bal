import ballerina/http;
import ballerina/log;

public function main() {
    log:printInfo("This is a Ballerina build project!");
}

service /hello on new http:Listener(9093) {

    resource function get satyHello() returns string {
        return "Hello!";
    }
}
