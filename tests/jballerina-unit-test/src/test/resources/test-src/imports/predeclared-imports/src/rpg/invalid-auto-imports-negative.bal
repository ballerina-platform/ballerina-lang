import ballerina/io;
import ballerina/lang.'decimal as 'xml;
import ballerina/lang.'int as 'error;
import ballerina/lang.'float as 'object;

function testConcat() returns xml {
    xml x = xml `<hello>xml content</hello>`;
    return 'xml:concat(x, "Hello", "hello from String");
}

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

function testErrorStackTrace('error:CallStackElement elem) returns string {
    return elem.callableName + ":" + elem.fileName;
}
