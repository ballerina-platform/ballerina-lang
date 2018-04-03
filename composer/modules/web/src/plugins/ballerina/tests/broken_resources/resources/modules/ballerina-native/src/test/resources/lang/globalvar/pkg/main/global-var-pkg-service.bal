package lang.globalvar.pkg.main;

import ballerina/lang.messages;
import ballerina/http;
import lang.globalvar.pkg.var;



int glbVarInt = 800;
string glbVarString = "value";
float glbVarFloat = 99.34323;
any glbVarAny = 88343;

float glbVarFloatChange = 99;

float glbVarFloat1 = var:glbVarFloat;

float glbVarFunc = functInv();

int glbVarPkgFunc = var:getIntValue();

@http:BasePath {value:"/globalvar-pkg"}
service GlobalVar {

    string serviceVarFloat = glbVarFloat;

    string serviceVarString = var:glbVarString;

    @http:GET{}
    @http:Path {value:"/defined"}
    resource accessGlobalVarFromOtherPkg (message m) {
        message response = {};
        int pkgInt = var:glbVarInt;
        string pkgString = var:glbVarString;
        float pkgFloat = var:glbVarFloat;
        json responseJson = {"glbVarInt":pkgInt, "glbVarString":pkgString, "glbVarFloat":pkgFloat};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/assign-from-other-pkg"}
    resource assignGlobalVarFromOtherPkg (message m) {
        message response = {};

        json responseJson = {"glbVarFloat1":glbVarFloat1};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/func-inv-from-same-pkg"}
    resource assignFuncInvocationToGlobalVarFromSamePkg (message m) {
        message response = {};
        json responseJson = {"glbVarFunc":glbVarFunc};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/func-inv-from-diff-pkg"}
    resource assignFuncInvocationToGlobalVarFromDiffPkg (message m) {
        message response = {};
        json responseJson = {"glbVarPkgFunc":glbVarPkgFunc};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/assign-to-service-var-from-diff-pkg"}
    resource assignGlobalVarToServiceVarFromDiffPkg (message m) {
        message response = {};
        json responseJson = {"serviceVarString":serviceVarString};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/change-global-var-diff-pkg"}
    resource changeGlobalVarInDiffPkg (message m) {
        message response = {};

        var:glbVarFloatChange = 345432.454;

        reply response;
    }



}


@http:BasePath {value:"/globalvar-second-pkg"}
service GlobalVarSecond {

    @http:GET{}
    @http:Path {value:"/get-changed-resource-level"}
    resource getChangedGlobalVarAtResourceLevel (message m) {
        message response = {};
        float changeVarFloat = var:glbVarFloatChange;
        json responseJson = {"changeVarFloat":changeVarFloat};
        messages:setJsonPayload(response, responseJson);
        reply response;
    }

}

function functInv() (float) {

    return 423277.72343;
}

