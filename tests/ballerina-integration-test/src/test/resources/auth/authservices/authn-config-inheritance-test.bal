import ballerina/http;
import ballerina/io;
import ballerina/auth;

endpoint http:APIListener listener01 {
    port:9091
};

@http:ServiceConfig {
    basePath:"/echo",
    authConfig:{
        authentication:{enabled:false},
        scopes:["xxx", "aaa"]
    }
}

service<http:Service> echo01 bind listener01 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test",
        authConfig:{
            authentication:{enabled:true}
        }
    }
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}
