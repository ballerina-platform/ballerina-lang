import ballerina/http;
import ballerina/log;

public function main() {
    log:printInfo("This is a Ballerina single file project!");
}

service /hello on new http:Listener(9093) {

    resource function get satyHello() returns string {
        return "Hello!";
    }
}
