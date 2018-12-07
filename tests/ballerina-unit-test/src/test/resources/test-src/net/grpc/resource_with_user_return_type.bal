// This is the server implementation for the unary blocking/unblocking scenario.
import ballerina/grpc;
import ballerina/io;

service HelloWorld on new grpc:Listener(9090) {

    resource function returnUserType(grpc:Caller caller, string name) returns Person {
        Person p = {};
        return p;
    }

    resource function returnTupleType(grpc:Caller caller, string name) returns (Person,grpc:Headers) {
        Person p = {};
        grpc:Headers headers = new;
        return (p,headers);
    }
}

type Person record {
   string name = "";
    int age = 0;
};
