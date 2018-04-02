import ballerina/lang.messages;
import ballerina/http;



int glbVarInt = 800;
string glbVarString = "value";
float glbVarFloat = 99.34323;
any glbVarAny = 88343;

float glbVarFloatChange = 99;

float glbVarFloat1 = glbVarFloat;


@http:BasePath {value:"/globalvar"}
service GlobalVar {

    string serviceVarFloat = glbVarFloat;

    @http:GET{}
    @http:Path {value:"/defined"}
    resource defineGlobalVar (message m) {
        message response = {};

        json responseJson = {"glbVarInt":glbVarInt, "glbVarString":glbVarString, "glbVarFloat":glbVarFloat};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/access-service-level"}
    resource accessGlobalVarAtServiceLevel (message m) {
        message response = {};

        json responseJson = {"serviceVarFloat":serviceVarFloat};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/change-resource-level"}
    resource changeGlobalVarAtResourceLevel (message m) {
        message response = {};
        glbVarFloatChange = 77.87;
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }


    @http:GET{}
    @http:Path {value:"/get-changed-resource-level"}
    resource getChangedGlobalVarAtResourceLevel (message m) {
        message response = {};
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

}


@http:BasePath {value:"/globalvar-second"}
service GlobalVarSecond {

    @http:GET{}
    @http:Path {value:"/get-changed-resource-level"}
    resource getChangedGlobalVarAtResourceLevel (message m) {
        message response = {};
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

}

