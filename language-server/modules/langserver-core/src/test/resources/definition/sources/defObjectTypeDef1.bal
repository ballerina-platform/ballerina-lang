import ballerina/http;
import ballerina/io;

public function testStdlibGotoDefinition() {
     http:Caller caller= new();
     http:Client cl = new("http://loaclhost:9090");
     var response = cl->get("/helloPath");
     io:println("Response Received");
}

public function testLangLig() {
    string[] stringArr = [];
    int length = stringArr.length();
}

public function testStdlibObjectFieldDefinition() {
    http:InboundAuthHandler[] authHandlerArr = [];
    http:InboundAuthHandlers authHandlers = authHandlerArr;
    http:AuthnFilter authFilter = new(authHandlers);
    authFilter.authHandlers = authHandlers;
}
