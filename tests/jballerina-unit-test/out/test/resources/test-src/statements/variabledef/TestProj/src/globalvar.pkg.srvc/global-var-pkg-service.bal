
import ballerina/http;
import globalvar.pkg.varpkg;


int glbVarInt = 800;
string glbVarString = "value";
float glbVarFloat = 99.34323;
any glbVarAny = 88343;

float glbVarFloatChange = 99.0;

float glbVarFloat1 = varpkg:getGlbVarFloat();

float glbVarFunc = functInv();

int glbVarPkgFunc = varpkg:getIntValue();

string serviceVarString = varpkg:getGlbVarString();

listener http:MockListener globalVarEP = new(9090);

@http:ServiceConfig {basePath:"/globalvar-pkg"}
service GlobalVar on globalVarEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/defined"
    }
    resource function accessGlobalVarFromOtherPkg (http:Caller conn, http:Request req) {
        int pkgInt = varpkg:getGlbVarInt();
        string pkgString = varpkg:getGlbVarString();
        float pkgFloat = varpkg:getGlbVarFloat();
        json responseJson = {"glbVarInt":pkgInt, "glbVarString":pkgString, "glbVarFloat":pkgFloat};

        http:Response res = new;
        res.setJsonPayload(responseJson);
        checkpanic conn->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/assign-from-other-pkg"
    }
    resource function assignGlobalVarFromOtherPkg (http:Caller conn, http:Request req) {

        http:Response res = new;
        json responseJson = {"glbVarFloat1":glbVarFloat1};
        res.setJsonPayload(responseJson);
        checkpanic conn->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/func-inv-from-same-pkg"
    }
    resource function assignFuncInvocationToGlobalVarFromSamePkg (http:Caller conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"glbVarFunc":glbVarFunc};
        res.setJsonPayload(responseJson);
        checkpanic conn->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/func-inv-from-diff-pkg"
    }
    resource function assignFuncInvocationToGlobalVarFromDiffPkg (http:Caller conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"glbVarPkgFunc":glbVarPkgFunc};
        res.setJsonPayload(responseJson);
        checkpanic conn->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/assign-to-service-var-from-diff-pkg"
    }
    resource function assignGlobalVarToServiceVarFromDiffPkg (http:Caller conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"serviceVarString":serviceVarString};
        res.setJsonPayload(responseJson);
        checkpanic conn->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/change-global-var-diff-pkg"
    }
    resource function changeGlobalVarInDiffPkg (http:Caller conn, http:Request req) {

        http:Response res = new;
        varpkg:setGlbVarFloatChange(345432.454);
        checkpanic conn->respond(res);
    }
}

@http:ServiceConfig {basePath:"/globalvar-second-pkg"}
service GlobalVarSecond on globalVarEP{

    @http:ResourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    resource function getChangedGlobalVarAtResourceLevel (http:Caller conn, http:Request req) {
        http:Response res = new;
        float changeVarFloat = varpkg:getGlbVarFloatChange();
        json responseJson = {"changeVarFloat":changeVarFloat};
        res.setJsonPayload(responseJson);
        checkpanic conn->respond(res);
    }

}

function functInv() returns (float) {
    return 423277.72343;
}
