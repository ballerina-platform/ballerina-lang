import ballerina.lang.messages as message;
import ballerina.lang.mock;
import ballerina.lang.test;
import ballerina.net.http;
import ballerina.lang.system;

function testMain () {
    message response = {};
    message request = {};
    string responseString;

    string myURL = test:startService("helloWorld");
    string mockURL = test:startService("mockService");
    mock:setValue("helloWorld.testConnector.param1", "new parameter2");
    mock:setValue("helloWorld.testConnector.terminalCon.param1", mockURL);

    http:ClientConnector varEP = create http:ClientConnector(myURL);
    message:setStringPayload(request, mockURL);
    response = http:ClientConnector.get(varEP, "/", request);
    responseString = message:getStringPayload(response);
    system:println("hello response: " + responseString);

}

connector TestConnector(string param1, string param2, int param3) {
    http:ClientConnector terminalCon = create http:ClientConnector("http://localhost:8080/original");

    action action1(TestConnector testConnector) (string){
        message req = {};
        message:setStringPayload(req, param1);
        system:println("param1 " + param1);
        message response = http:ClientConnector.get(terminalCon, "/", req);
        system:println("response " + message:getStringPayload(response));
        return message:getStringPayload(response);
    }
}


@http:BasePath ("/hello")
service helloWorld {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);

    @http:GET
    resource sayHello(message m) {
        string action1;
        message beRep = {};
        message response = {};
        string ep = "";
        ep = message:getStringPayload(m);

        action1 = TestConnector.action1(testConnector);
        message:setStringPayload(response, action1);
        reply response;
    }
}

@http:BasePath ("/mock")
service mockService {
    @http:GET
    resource mockResource(message m) {
        message resp = {};
        message:setStringPayload(resp, "You invoked mockService!");
        reply resp;
    }
}
