import ballerina/http;
import ballerina/io;
import ballerina/auth;

http:AuthProvider basicAuthProvider05 = {
    scheme:"basic",
    authStoreProvider:"config"
};

listener http:Listener listener05 = new(9094, config = {
    authProviders:[basicAuthProvider05],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath:"/echo",
    authConfig:{
        authentication:{enabled:true},
        scopes:["scope2"]
    }
}
service echo05 on listener05 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    resource function echo(http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/path/{id}"
    }
    resource function path(http:Caller caller, http:Request req, string id) {
        http:Response res = new;
        _ = caller->respond(res);
    }
}
