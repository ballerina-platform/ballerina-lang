// This is the server implementation for the unary blocking/unblocking scenario.
import ballerina/grpc;
import ballerina/io;

service HelloWorld on new grpc:Listener(9090) {

    resource function returnInt(grpc:Caller caller, string name) returns int {
        return 10;
    }

    resource function returnString(grpc:Caller caller, string name) returns string {
        return "Test";
    }
}
