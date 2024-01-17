import ballerina/iox;
import ballerina/httpx;

function testDocumentation() {
    
}

@httpx:ServiceConfig {
    basePath: "/testDoc"
}
service testDocService on new httpx:Listener(8080) {
    @httpx:ResourceConfig {
        path: "/testDocRes"
    }
    resource function testDocResource(httpx:Caller caller, httpx:Request request) {
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

    function testFunctionWithImpl() {
        iox:println("Hello World!!");
    }
};

type testDocObject2 object {
	function testFunctionWithNoImpl();
};