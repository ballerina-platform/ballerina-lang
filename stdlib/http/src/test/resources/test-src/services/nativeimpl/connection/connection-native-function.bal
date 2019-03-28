import ballerina/http;

listener http:MockListener mockEP = new(9090);

service hello on mockEP {
    @http:ResourceConfig {
        path:"/redirect",
        methods:["GET"]
    }
    resource function redirect (http:Caller caller, http:Request req) {
        http:Response res = new;
        checkpanic caller->redirect(res, http:REDIRECT_MOVED_PERMANENTLY_301, ["location1"]);
    }
}
