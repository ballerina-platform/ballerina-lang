import ballerina/lang.'object as lang;
type Annot record {
    string basePath;
};

type Annot2 record {
    string path;
};

annotation Annot ServiceConfig on service;
annotation Annot2 ResourceConfig on resource function;

function testDocumentation() {
    
}

@ServiceConfig {
    basePath: "/testHello"
}
service helloService on new MockListener(8080) {

    @ResourceConfig {
        path: "/sayHello"
    }
    resource function sayHello(MockCaller caller, MockRequest request) {
        //io:println("Hello World!!");
    }
}

type testDocRecord record {
    int field1 = 12;
    string field2 = "hello";
};

class testDocObject {
    int testField = 12;
    private int testPrivate = 12;
    public string testString = "hello";

    function testFunctionWithImpl() {
        // io:println("Hello World!!");
    }
}

type testDocAbstractObject object {
    function testFunction();
};

client class MockCaller {
    function __init() {
        
    }
}

class MockRequest {

}

class MockListener {
    *lang:Listener;
    function init(int port) {
        
    }

    public function attach(service s, string? name = ()) returns error? {
    }

    public function __detach(service s) returns error? {
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
        return ();
    }

    public function immediateStop() returns error? {
        return ();
    }
}

final int testModuleVar2 = 10;

@varAnnotation
final int testModuleVar1 = 10;

annotation varAnnotation;

const string constString = "Hello";

enum Color {
    RED,
    GREEN,
    BLUE
}
