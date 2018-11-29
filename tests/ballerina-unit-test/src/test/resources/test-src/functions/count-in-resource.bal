import ballerina/http;
import ballerina/io;

@http:ServiceConfig {
    basePath:"/test"
}
service TestService on new http:Listener(80) {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/resource"
    }
    resource function testResource (http:Caller caller, http:Request req) {
        json[] jsonArray = [];
        string[] strArray = ["foo", "bar"];
        foreach s in strArray {
            jsonArray[jsonArray.count()] = s;
        }

        http:Response res = new;
        res.setJsonPayload(untaint jsonArray);
        _ = caller -> respond(res);
    }
}
