function testCodeActionFunction() {
    io:println("Hello World!!");
}

service testCodeActionService on new http:Listener(8080) {
    resource function testCodeActionResource(http:Caller caller, http:Request request) {
        http:Response res = new;
        checkpanic caller->respond(res);
    }
}

type testCodeActionObject object {
    int testField = 12;
    private int testPrivate = 12;
    public string testString = "hello";

    function testFunctionSignature();
    function testFunctionWithImpl() {
        io:println("Hello World!!");
    }
};
