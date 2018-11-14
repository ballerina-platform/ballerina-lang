import ballerina/http;
import ballerina/auth;

http:AuthProvider basicAuthProvider04 = {id: "basic1", scheme:"basic", authStoreProvider:"config"};
endpoint http:SecureListener listener04 {
    port:9096,
    authProviders:[basicAuthProvider04]
};

@http:ServiceConfig {
    basePath:"/echo",
    authConfig:{
        authProviders:["basic2"],
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