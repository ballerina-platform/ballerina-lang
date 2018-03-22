package mock;

import ballerina/lang.system;
import ballerina/lang.messages;
import ballerina/net.http;

connector TestConnector(string param1, string param2, int param3) {
    http:ClientConnector terminalCon = create http:ClientConnector("http://localhost:8080/original");

    action action1() (string){
        message req = {};
        messages:setStringPayload(req, param1);
        system:println("param1 " + param1);
        message response = terminalCon.get("/", req);
        system:println("response " + messages:getStringPayload(response));
        return messages:getStringPayload(response);
    }
}

