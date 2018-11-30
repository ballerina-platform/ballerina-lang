import ballerina/http;
import ballerina/io;
import ballerina/auth;

http:AuthProvider basicAuthProvider04 = {
    scheme:"basic",
    authStoreProvider:"config"
};

listener http:Listener listener04 = new(9093, config = {
    authProviders:[basicAuthProvider04],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath:"/echo"
}
service echo04 on listener04 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test",
        authConfig:{
            authentication:{enabled:true},
            scopes:["scope2"]
        }
    }
    resource function echo (http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}
