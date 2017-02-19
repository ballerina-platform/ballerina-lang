package mock;

import ballerina.lang.messages as message;

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

