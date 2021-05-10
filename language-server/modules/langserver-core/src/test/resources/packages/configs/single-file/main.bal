import ballerina/io;
import ballerina/http;

public function main() {
    io:println("Hello!");
}

service /hey on new http:Listener(9093) {

    resource function get satyHello() returns string {
        return "Hey!";
    }
}
