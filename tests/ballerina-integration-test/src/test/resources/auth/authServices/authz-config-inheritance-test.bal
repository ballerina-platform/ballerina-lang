import ballerina/http;
import ballerina/io;
import ballerina/auth;

endpoint http:APIListener listener02 {
    port:9092
};

@http:ServiceConfig {
    basePath:"/echo",
    authConfig:{
        authentication:{enabled:true},
        scopes:["xxx"]
    }
}
service<http:Service> echo02 bind listener02 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test",
        authConfig:{
            scopes:["scope2", "scope4"]
        }
    }
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}
