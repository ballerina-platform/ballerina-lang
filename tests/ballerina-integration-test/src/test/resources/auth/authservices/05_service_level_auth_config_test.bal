import ballerina/http;
import ballerina/io;
import ballerina/auth;

http:AuthProvider basicAuthProvider05 = {
    scheme:"basic",
    authStoreProvider:"config"
};

endpoint http:Listener listener04 {
    port:9094,
    authProviders:[basicAuthProvider05]
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
