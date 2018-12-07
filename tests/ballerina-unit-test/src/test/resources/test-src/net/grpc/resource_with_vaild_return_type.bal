// This is the server implementation for the unary blocking/unblocking scenario.
import ballerina/grpc;
import ballerina/io;

service HelloWorld on new grpc:Listener(9090) {

    resource function returnError(grpc:Caller caller, string name) returns error? {
        error err = error("Return Error");
        return err;
    }

    resource function returnNil(grpc:Caller caller, string name) returns error? {
        return;
    }

    resource function noReturn(grpc:Caller caller, string name) {
    }
}
