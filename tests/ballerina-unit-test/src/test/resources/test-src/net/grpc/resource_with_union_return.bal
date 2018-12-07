// This is the server implementation for the unary blocking/unblocking scenario.
import ballerina/grpc;
import ballerina/io;

service HelloWorld on new grpc:Listener(9090) {

    resource function returnUnion(grpc:Caller caller, string name) returns string|error {
        error err = error("Return Error");
        return err;
    }
}
