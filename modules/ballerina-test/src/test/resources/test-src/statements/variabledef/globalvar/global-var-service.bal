import ballerina.net.http;

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
    resource defineGlobalVar (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"glbVarInt":glbVarInt, "glbVarString":glbVarString, "glbVarFloat":glbVarFloat};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/access-service-level"
    }
    resource accessGlobalVarAtServiceLevel (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"serviceVarFloat":serviceVarFloat};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/change-resource-level"
    }
    resource changeGlobalVarAtResourceLevel (http:Connection conn, http:Request req) {
        http:Response res = {};
        glbVarFloatChange = 77.87;
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    resource getChangedGlobalVarAtResourceLevel (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

}


@http:configuration {basePath:"/globalvar-second"}
service<http> GlobalVarSecond {

    @http:resourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    resource getChangedGlobalVarAtResourceLevel (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"glbVarFloatChange":glbVarFloatChange};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

}

