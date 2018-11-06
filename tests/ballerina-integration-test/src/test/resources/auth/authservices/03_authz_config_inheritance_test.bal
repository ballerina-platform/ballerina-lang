import ballerina/http;
import ballerina/io;
import ballerina/auth;

http:AuthProvider basicAuthProvider03 = {
    scheme:"basic",
    authStoreProvider:"config"
};

endpoint http:Listener listener03 {
    port:9092,
    authProviders:[basicAuthProvider03],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

@http:ServiceConfig {
    basePath:"/echo",
    authConfig:{
        authentication:{enabled:true},
        scopes:["xxx"]
    }
}
service<http:Service> echo03 bind listener03 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test",
        authConfig:{
            scopes:["scope2", "scope4"]
        }
    }
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}
