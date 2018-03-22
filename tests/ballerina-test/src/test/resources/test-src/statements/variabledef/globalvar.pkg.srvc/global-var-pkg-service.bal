package globalvar.pkg.srvc;

import ballerina/net.http;
import globalvar.pkg.varpkg;


int glbVarInt = 800;
string glbVarString = "value";
float glbVarFloat = 99.34323;
any glbVarAny = 88343;

float glbVarFloatChange = 99;

float glbVarFloat1 = varpkg:glbVarFloat;

float glbVarFunc = functInv();

int glbVarPkgFunc = varpkg:getIntValue();

@http:configuration {basePath:"/globalvar-pkg"}
service<http> GlobalVar {

    string serviceVarFloat = <string> glbVarFloat;

    string serviceVarString = varpkg:glbVarString;

    @http:resourceConfig {
        methods:["GET"],
        path:"/defined"
    }
    resource accessGlobalVarFromOtherPkg (http:Connection conn, http:Request req) {
        int pkgInt = varpkg:glbVarInt;
        string pkgString = varpkg:glbVarString;
        float pkgFloat = varpkg:glbVarFloat;
        json responseJson = {"glbVarInt":pkgInt, "glbVarString":pkgString, "glbVarFloat":pkgFloat};

        http:Response res = {};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/assign-from-other-pkg"
    }
    resource assignGlobalVarFromOtherPkg (http:Connection conn, http:Request req) {

        http:Response res = {};
        json responseJson = {"glbVarFloat1":glbVarFloat1};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/func-inv-from-same-pkg"
    }
    resource assignFuncInvocationToGlobalVarFromSamePkg (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"glbVarFunc":glbVarFunc};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/func-inv-from-diff-pkg"
    }
    resource assignFuncInvocationToGlobalVarFromDiffPkg (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"glbVarPkgFunc":glbVarPkgFunc};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/assign-to-service-var-from-diff-pkg"
    }
    resource assignGlobalVarToServiceVarFromDiffPkg (http:Connection conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"serviceVarString":serviceVarString};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/change-global-var-diff-pkg"
    }
    resource changeGlobalVarInDiffPkg (http:Connection conn, http:Request req) {

        http:Response res = {};
        varpkg:glbVarFloatChange = 345432.454;
        _ = conn.respond(res);
    }



}


@http:configuration {basePath:"/globalvar-second-pkg"}
service<http> GlobalVarSecond {

    @http:resourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    resource getChangedGlobalVarAtResourceLevel (http:Connection conn, http:Request req) {
        http:Response res = {};
        float changeVarFloat = varpkg:glbVarFloatChange;
        json responseJson = {"changeVarFloat":changeVarFloat};
        res.setJsonPayload(responseJson);
        _ = conn.respond(res);
    }

}

function functInv() (float) {

    return 423277.72343;
}

