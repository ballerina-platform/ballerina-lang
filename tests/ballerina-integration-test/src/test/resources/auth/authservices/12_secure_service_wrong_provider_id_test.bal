import ballerina/http;
import ballerina/auth;

http:AuthProvider basicAuthProvider12 = {
    id: "basic1",
    scheme:"basic",
    authStoreProvider:"config"
};

endpoint http:Listener listener12 {
    port:9194,
    authProviders:[basicAuthProvider12],
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
        authProviders:["basic2"],
        authentication:{enabled:true},
        scopes:["scope2"]
    }
}
service<http:Service> echo12 bind listener12 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}