import ballerina/iox;
import ballerina/httpx;

function testDocumentation() {
    
}

service / on new httpx:Listener(8080) {
    resource function get getResource(httpx:Caller caller, httpx:Request req) {
        httpx:Response res = new;
        checkpanic caller->respond(res);
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

    public function testFunctionWithImpl();
};

type testDocObject2 object {
    
    function testFunctionSignature(int param1, string param2) returns int;
};

final int testModuleVar2 = 10;

@varAnnotation
final int testModuleVar1 = 10;

annotation varAnnotation;
