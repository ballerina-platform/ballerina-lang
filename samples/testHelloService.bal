import ballerina.lang.message;
import ballerina.lang.mock;
import ballerina.net.http;
import ballerina.lang.system;

function testMain () {
    message response = {};
    message request = {};
    string responseString;

    string myURL = mock:startService("helloWorld");
    string mockURL = mock:startService("mockService");
    mock:setValue("helloWorld.testConnector.param1", "new parameter2");
    mock:setValue("helloWorld.testConnector.terminalCon.param1", mockURL);

    http:HTTPConnector varEP = create http:HTTPConnector(myURL);
    message:setStringPayload(request, mockURL);
    response = http:HTTPConnector.get(varEP, "/", request);
    responseString = message:getStringPayload(response);
    system:println("hello response: " + responseString);

}

connector TestConnector(string param1, string param2, int param3) {
    http:HTTPConnector terminalCon = create http:HTTPConnector("http://localhost:8080/original");

    action action1(TestConnector testConnector) (string){
        message req = {};
        message:setStringPayload(req, param1);
        system:println("param1 " + param1);
        message response = http:HTTPConnector.get(terminalCon, "/", req);
        system:println("response " + message:getStringPayload(response));
        return message:getStringPayload(response);
    //return param1;
    }
}


@BasePath ("/hello")
service helloWorld {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);

    @GET
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

@BasePath ("/mock")
service mockService {
    @GET
    resource mockResource(message m) {
        message resp = {};
        message:setStringPayload(resp, "You invoked mockService!");
        reply resp;
    }
}
