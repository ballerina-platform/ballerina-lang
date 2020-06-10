import ballerina/io;

listener  CustomListener lstnr = new CustomListener();

type CustomListener object {
    *'object:Listener;

    public function __attach(service s, string? name) returns error? {
        io:println("running __attach");
    }

    public function __detach(service s) returns error? {
        io:println("running __dettach");
    }

    public function __start() returns error? {
        io:println("running __start");
    }

    public function __gracefulStop() returns error? {
        io:println("running __gracefulStop");
    }

    public function __immediateStop() returns error? {
        io:println("running __immediateStop");
    }
};
