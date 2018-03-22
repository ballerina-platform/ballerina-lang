package mock;

import ballerina/lang.messages;
import ballerina/net.http;

@http:configuration {basePath:"/mock"}
service<http> mockService {

    @http:resourceConfig{
        methods:["GET"],
        path:"/"
    }
    resource mockResource(message m) {
        message resp = {};
        messages:setStringPayload(resp, "You invoked mockService!");
        reply resp;
    }
}
