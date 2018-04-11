import ballerina/http;

int glbVarInt = 800;
string glbVarString = "value";
float glbVarFloat = 99.34323;
any glbVarAny = 88343;

float glbVarFloatChange = 99;

float glbVarFloat1 = glbVarFloat;

endpoint http:Listener echoEP {
    port:9090
};

@http:ServiceConfig {basePath:"/globalvar"}
service<http:Service> GlobalVar bind echoEP {

    string serviceVarFloat = <string>glbVarFloat;

    @http:ResourceConfig {
        methods:["GET"],
        path:"/defined"
    }
    defineGlobalVar (endpoint conn, http:Request request) {
        http:Response res = new;
        json responseJson = {"glbVarInt":glbVarInt, "glbVarString":glbVarString, "glbVarFloat":glbVarFloat};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/access-service-level"
    }
    accessGlobalVarAtServiceLevel (endpoint conn, http:Request request) {
        http:Response res = new;
        json responseJson = {"serviceVarFloat":serviceVarFloat};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/change-resource-level"
    }
    changeGlobalVarAtResourceLevel (endpoint conn, http:Request request) {
        http:Response res = new;
        glbVarFloatChange = 77.87;
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    getChangedGlobalVarAtResourceLevel (endpoint conn, http:Request request) {
        http:Response res = new;
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

}

@http:ServiceConfig {basePath:"/globalvar-second"}
service<http:Service> GlobalVarSecond bind echoEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    getChangedGlobalVarAtResourceLevel (endpoint conn, http:Request request) {
        http:Response res = new;
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

}

