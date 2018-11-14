import ballerina/http;
import ballerina/io;
import ballerina/auth;

endpoint http:APIListener listener04 {
    port:9094
};

@http:ServiceConfig {
    basePath:"/echo",
    authConfig:{
        authentication:{enabled:true},
        scopes:["scope2"]
    }
}
service<http:Service> echo04 bind listener04 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}
