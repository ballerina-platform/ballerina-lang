package mock;

import ballerina.lang.messages;
import ballerina.net.http;

@http:BasePath  {value: "/hello"}
service helloWorld {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);

    @http:GET{}
    @http:Path{ value: "/"}
    resource sayHello(message m) {
        string action1;
        message beRep = {};
        message response = {};
        string ep = messages:getStringPayload(m);
        action1 = TestConnector.action1(testConnector);
        messages:setStringPayload(response, action1);
        reply response;
    }
}