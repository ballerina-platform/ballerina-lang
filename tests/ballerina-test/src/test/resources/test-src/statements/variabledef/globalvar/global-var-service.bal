import ballerina.net.http;
import ballerina.net.http.mock;

int glbVarInt = 800;
string glbVarString = "value";
float glbVarFloat = 99.34323;
any glbVarAny = 88343;

float glbVarFloatChange = 99;

float glbVarFloat1 = glbVarFloat;

endpoint<mock:NonListeningService> echoEP {
    port:9090
}

@http:serviceConfig {basePath:"/globalvar", endpoints:[echoEP]}
service<http:Service> GlobalVar {

    string serviceVarFloat = <string>glbVarFloat;

    @http:resourceConfig {
        methods:["GET"],
        path:"/defined"
    }
    resource defineGlobalVar (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"glbVarInt":glbVarInt, "glbVarString":glbVarString, "glbVarFloat":glbVarFloat};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/access-service-level"
    }
    resource accessGlobalVarAtServiceLevel (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"serviceVarFloat":serviceVarFloat};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/change-resource-level"
    }
    resource changeGlobalVarAtResourceLevel (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        glbVarFloatChange = 77.87;
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    resource getChangedGlobalVarAtResourceLevel (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

}

@http:serviceConfig {basePath:"/globalvar-second", endpoints:[echoEP]}
service<http:Service> GlobalVarSecond {

    @http:resourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    resource getChangedGlobalVarAtResourceLevel (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

}

