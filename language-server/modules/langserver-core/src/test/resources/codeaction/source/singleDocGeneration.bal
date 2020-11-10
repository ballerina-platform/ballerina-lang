import ballerina/io;
import ballerina/http;

function testDocumentation() {
    
}

@http:ServiceConfig {
    basePath: "/testHello"
}
service helloService on new http:Listener(8080) {

    @http:ResourceConfig {
        path: "/sayHello"
    }
    resource function sayHello(http:Caller caller, http:Request request) {
        io:println("Hello World!!");
    }
}

type testDocRecord record {
    int field1 = 12;
    string field2 = "hello";
};

type testDocObject object {
    int testField = 12;
    private int testPrivate = 12;
    public string testString = "hello";

    function testFunctionWithImpl() {
        io:println("Hello World!!");
    }
};

type testDocAbstractObject object {
    function testFunction();
};