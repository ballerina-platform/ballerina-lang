import ballerina/http;
import ballerina/io;
import ballerina/auth;

http:AuthProvider basicAuthProvider05 = {
    scheme:"basic",
    authStoreProvider:"config"
};

endpoint http:Listener listener05 {
    port:9094,
    authProviders:[basicAuthProvider05],
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
        scopes:["scope2"]
    }
}
service<http:Service> echo05 bind listener05 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/path/{id}"
    }
    path(endpoint caller, http:Request req, string id) {
        http:Response res = new;
        _ = caller->respond(res);
    }
}
