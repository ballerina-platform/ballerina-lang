import ballerina/http;
import ballerina/io;

endpoint http:APIListener listener {
    port:9090
};

@http:ServiceConfig {
    basePath:"/echo",
    authConfig:{
        authentication:{enabled:true},
        scopes:["xxx", "aaa"]
    }
}

service<http:Service> echo bind listener {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test",
        authConfig:{
            authentication:{enabled:false}
        }
    }
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}

