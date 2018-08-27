function testCodeActionFunction() {
    io:println("Hello World!!");
}

endpoint http:Listener listener {
    
};

service<http:Service> testCodeActionService bind { port: 9090 } {
    testCodeActionResource (endpoint caller, http:Request request) {
        http:Response res = new;
        _ = caller->respond(res);
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
