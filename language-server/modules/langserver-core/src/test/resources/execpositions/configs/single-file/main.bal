import ballerina/httpx;
import ballerina/logx;

public function main() {
    logx:printInfo("This is a Ballerina single file project!");
}

service /hello on new httpx:Listener(9093) {

    resource function get satyHello() returns string {
        return "Hello!";
    }
}
