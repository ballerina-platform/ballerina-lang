package mock;

import ballerina/lang.messages;
import ballerina/net.http;

@http:configuration {basePath:"/hello"}
service<http> helloWorld {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);

    @http:resourceConfig{
        methods:["GET"],
        path:"/"
    }
    resource sayHello(message m) {
        string action1;
        message beRep = {};
        message response = {};
        string ep = messages:getStringPayload(m);
        action1 = testConnector.action1();
        messages:setStringPayload(response, action1);
        reply response;
    }
}