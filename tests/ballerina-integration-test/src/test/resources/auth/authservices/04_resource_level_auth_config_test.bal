import ballerina/http;
import ballerina/io;
import ballerina/auth;

http:AuthProvider basicAuthProvider04 = {
    scheme:"basic",
    authStoreProvider:"config"
};

endpoint http:Listener listener03 {
    port:9093,
    authProviders:[basicAuthProvider04]
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo03 bind listener03 {
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
