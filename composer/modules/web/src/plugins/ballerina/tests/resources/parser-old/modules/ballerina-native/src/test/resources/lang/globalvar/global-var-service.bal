import ballerina/lang.messages;
import ballerina/http;



int glbVarInt = 800;
string glbVarString = "value";
float glbVarFloat = 99.34323;
any glbVarAny = 88343;

float glbVarFloatChange = 99;

float glbVarFloat1 = glbVarFloat;


@http:configuration {basePath:"/globalvar"}
service<http> GlobalVar {

    string serviceVarFloat = <string>glbVarFloat;

    @http:resourceConfig {
        methods:["GET"],
        path:"/defined"
    }
    resource defineGlobalVar (message m) {
        message response = {};

        json responseJson = {"glbVarInt":glbVarInt, "glbVarString":glbVarString, "glbVarFloat":glbVarFloat};
        messages:setJsonPayload(response, responseJson);
        response:send(response);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/access-service-level"
    }
    resource accessGlobalVarAtServiceLevel (message m) {
        message response = {};

        json responseJson = {"serviceVarFloat":serviceVarFloat};
        messages:setJsonPayload(response, responseJson);
        response:send(response);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/change-resource-level"
    }
    resource changeGlobalVarAtResourceLevel (message m) {
        message response = {};
        glbVarFloatChange = 77.87;
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        messages:setJsonPayload(response, responseJson);
        response:send(response);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    resource getChangedGlobalVarAtResourceLevel (message m) {
        message response = {};
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        messages:setJsonPayload(response, responseJson);
        response:send(response);
    }

}


@http:configuration {basePath:"/globalvar-second"}
service<http> GlobalVarSecond {

    @http:resourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    resource getChangedGlobalVarAtResourceLevel (message m) {
        message response = {};
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        messages:setJsonPayload(response, responseJson);
        response:send(response);
    }

}

