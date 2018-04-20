import ballerina/http;
import ballerina/auth;

http:AuthProvider basicAuthProvider = {scheme:"basic", authProvider:"config"};
endpoint http:SecureListener listener {
    port:9090,
    authProviders:[basicAuthProvider]
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind listener {
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
