import ballerina/http;
import ballerina/io;

http:AuthProvider basicAuthProvider01 = {
    scheme:"basic",
    authStoreProvider:"config"
};

endpoint http:Listener listener {
    port:9090,
    authProviders:[basicAuthProvider01]
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

