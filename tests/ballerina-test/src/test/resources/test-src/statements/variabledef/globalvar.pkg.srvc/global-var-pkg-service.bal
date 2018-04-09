package globalvar.pkg.srvc;

import ballerina/http;
import globalvar.pkg.varpkg;


int glbVarInt = 800;
string glbVarString = "value";
float glbVarFloat = 99.34323;
any glbVarAny = 88343;

float glbVarFloatChange = 99;

float glbVarFloat1 = varpkg:glbVarFloat;

float glbVarFunc = functInv();

int glbVarPkgFunc = varpkg:getIntValue();

endpoint http:NonListener globalVarEP {
    port:9090
};

@http:ServiceConfig {basePath:"/globalvar-pkg"}
service<http:Service> GlobalVar bind globalVarEP {

    string serviceVarFloat = <string> glbVarFloat;

    string serviceVarString = varpkg:glbVarString;

    @http:ResourceConfig {
        methods:["GET"],
        path:"/defined"
    }
    accessGlobalVarFromOtherPkg (endpoint conn, http:Request req) {
        int pkgInt = varpkg:glbVarInt;
        string pkgString = varpkg:glbVarString;
        float pkgFloat = varpkg:glbVarFloat;
        json responseJson = {"glbVarInt":pkgInt, "glbVarString":pkgString, "glbVarFloat":pkgFloat};

        http:Response res = new;
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/assign-from-other-pkg"
    }
    assignGlobalVarFromOtherPkg (endpoint conn, http:Request req) {

        http:Response res = new;
        json responseJson = {"glbVarFloat1":glbVarFloat1};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/func-inv-from-same-pkg"
    }
    assignFuncInvocationToGlobalVarFromSamePkg (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"glbVarFunc":glbVarFunc};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/func-inv-from-diff-pkg"
    }
    assignFuncInvocationToGlobalVarFromDiffPkg (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"glbVarPkgFunc":glbVarPkgFunc};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/assign-to-service-var-from-diff-pkg"
    }
    assignGlobalVarToServiceVarFromDiffPkg (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"serviceVarString":serviceVarString};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/change-global-var-diff-pkg"
    }
    changeGlobalVarInDiffPkg (endpoint conn, http:Request req) {

        http:Response res = new;
        varpkg:glbVarFloatChange = 345432.454;
        _ = conn -> respond(res);
    }



}


@http:ServiceConfig {basePath:"/globalvar-second-pkg"}
service<http:Service> GlobalVarSecond bind globalVarEP{

    @http:ResourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    getChangedGlobalVarAtResourceLevel (endpoint conn, http:Request req) {
        http:Response res = new;
        float changeVarFloat = varpkg:glbVarFloatChange;
        json responseJson = {"changeVarFloat":changeVarFloat};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

}

function functInv() returns (float) {

    return 423277.72343;
}

