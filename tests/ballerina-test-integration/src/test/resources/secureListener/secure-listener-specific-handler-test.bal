import ballerina/http;
import ballerina/auth;

http:AuthProvider basicAuthProvider = {id: "basic1", scheme:"basic", authProvider:"config"};
endpoint http:SecureListener listener {
    port:9090,
    authProviders:[basicAuthProvider]
};

@http:ServiceConfig {
    basePath:"/echo",
    authConfig:{
        authProviders:["basic1"],
        authentication:{enabled:true},
        scopes:["scope2"]
    }
}
service<http:Service> echo bind listener {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}
