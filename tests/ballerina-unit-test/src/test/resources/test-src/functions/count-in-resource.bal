import ballerina/http;
import ballerina/io;
import ballerina/crypto;

listener http:MockListener testEP = new(9090);

@http:ServiceConfig {
    basePath:"/test"
}
service TestService on testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/resource"
    }
    resource function testResource (http:Caller caller, http:Request req) {
        json[] jsonArray = [];
        string[] strArray = ["foo", "bar"];
        foreach var s in strArray {
            jsonArray[jsonArray.count()] = s;
        }

        http:Response res = new;
        res.setJsonPayload(<json[]>crypto:unsafeMarkUntainted(jsonArray));
        _ = caller -> respond(res);
    }
}
