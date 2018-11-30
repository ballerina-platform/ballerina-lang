import ballerina/http;
import testorg/foo version v1;

listener http:MockListener echoEP  = new(9090);

@http:ServiceConfig {basePath:"/globalvar"}
service GlobalVar on echoEP {

    string serviceVarFloat = <string> foo:glbVarFloat;

    @http:ResourceConfig {
        methods:["GET"],
        path:"/defined"
    }
    resource function defineGlobalVar (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"glbVarInt":foo:glbVarInt, "glbVarString":foo:glbVarString, "glbVarFloat":foo:glbVarFloat};
        res.setJsonPayload(responseJson);
        _ = caller -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/access-service-level"
    }
    resource function accessGlobalVarAtServiceLevel (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"serviceVarFloat": self.serviceVarFloat};
        res.setJsonPayload(responseJson);
        _ = caller -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/change-resource-level"
    }
    resource function changeGlobalVarAtResourceLevel (http:Caller caller, http:Request req) {
        http:Response res = new;
        foo:glbVarFloatChange = 77.87;
        json responseJson = {"glbVarFloatChange":foo:glbVarFloatChange};
        res.setJsonPayload(responseJson);
        _ = caller -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/get-changed-resource-level"
    }
    resource function getChangedGlobalVarAtResourceLevel (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"glbVarFloatChange":foo:glbVarFloatChange};
        res.setJsonPayload(responseJson);
        _ = caller -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/arrays"
    }
    resource function getGlobalArraysAtResourceLevel (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {
            "glbArrayElement":foo:glbArray[0],
            "glbSealedArrayElement":foo:glbSealedArray[1],
            "glbSealedArray2Element":foo:glbSealedArray2[2],
            "glbSealed2DArrayElement":foo:glbSealed2DArray[0][0],
            "glbSealed2DArray2Element":foo:glbSealed2DArray2[0][1]
        };
        res.setJsonPayload(responseJson);
        _ = caller -> respond(res);
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
        json responseJson = {"glbVarFloatChange":foo:glbVarFloatChange};
        res.setJsonPayload(responseJson);
        _ = caller -> respond(res);
    }

}

