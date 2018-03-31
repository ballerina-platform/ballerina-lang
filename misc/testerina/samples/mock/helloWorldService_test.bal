package mock;

import ballerina/lang.messages;
import ballerina/mock;
import ballerina/test;
import ballerina/net.http;
import ballerina/lang.system;

function testMain () {
    message response = {};
    message request = {};
    string responseString;

    string myURL = test:startService("helloWorld");
    string mockURL = test:startService("mockService");

    mock:setValue("helloWorld.testConnector.param1", "new parameter2");
    mock:setValue("helloWorld.testConnector.terminalCon.param1", mockURL);

    http:ClientConnector varEP = create http:ClientConnector(myURL);
    messages:setStringPayload(request, mockURL);
    response = varEP.get("/", request);

    responseString = messages:getStringPayload(response);
    system:println("hello response: " + responseString);

    test:assertStringEquals(responseString, "You invoked mockService!", "");
}