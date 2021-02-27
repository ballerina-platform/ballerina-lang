import ballerina/lang.'object as lang;
import ballerina/io;

listener  CustomListener lstnr = new CustomListener();

public function main(string... args) {
    io:println("running main");
}

class CustomListener {
    *lang:Listener;

    public function attach(service object {} s, string[]? name) returns error? {
        io:println("running __attach");
    }

    public function detach(service object {} s) returns error? {
        io:println("running __dettach");
    }

    public function 'start() returns error? {
        io:println("running __start");
    }

    public function gracefulStop() returns error? {
        io:println("running __gracefulStop");
    }

    public function immediateStop() returns error? {
        io:println("running __immediateStop");
    }
}
