import ballerina/http;
import testorg/foo;

listener http:MockListener echoEP  = new(9090);

@http:ServiceConfig {basePath:"/globalvar"}
service GlobalVar on echoEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/defined"
    }
    resource function defineGlobalVar (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {
            "glbVarInt": foo:getGlbVarInt(),
            "glbVarString": foo:getGlbVarString(),
            "glbVarFloat": foo:getGlbVarFloat()
        };
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/change-resource-level"
    }
    resource function changeGlobalVarAtResourceLevel (http:Caller caller, http:Request req) {
        http:Response res = new;
        foo:setGlbVarFloatChange(77.87);
        json responseJson = { "glbVarFloatChange": foo:getGlbVarFloatChange() };
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    resource function getChangedGlobalVarAtResourceLevel (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = { "glbVarFloatChange": foo: getGlbVarFloatChange() };
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/arrays"
    }
    resource function getGlobalArraysAtResourceLevel (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {
            "glbArrayElement": foo:getGlbArray()[0],
            "glbSealedArrayElement": foo:getGlbSealedArray()[1],
            "glbSealedArray2Element": foo:getGlbSealedArray2()[2],
            "glbSealed2DArrayElement": foo:getGlbSealed2DArray()[0][0],
            "glbSealed2DArray2Element": foo:getGlbSealed2DArray2()[0][1]
        };
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {basePath:"/globalvar-second"}
service GlobalVarSecond on echoEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    resource function getChangedGlobalVarAtResourceLevel (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = { "glbVarFloatChange": foo: getGlbVarFloatChange() };
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

}

