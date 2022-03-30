import ballerina/http;

service /hello on new http:Listener(9090) {
    resource function get . (http:Caller caller) returns string {
        return "Hello World!";
    }
}
