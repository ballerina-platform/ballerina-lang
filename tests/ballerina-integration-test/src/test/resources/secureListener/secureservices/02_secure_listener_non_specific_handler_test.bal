import ballerina/http;
import ballerina/auth;

http:AuthProvider basicAuthProvider01 = {scheme:"basic", authStoreProvider:"config"};
endpoint http:SecureListener listener01 {
    port:9091,
    authProviders:[basicAuthProvider01]
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind listener01 {
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
