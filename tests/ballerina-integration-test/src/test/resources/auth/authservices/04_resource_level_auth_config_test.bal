import ballerina/http;
import ballerina/io;
import ballerina/auth;

endpoint http:APIListener listener03 {
    port:9093
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo03 bind listener03 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test",
        authConfig:{
            authentication:{enabled:true},
            scopes:["scope2"]
        }
    }
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}
