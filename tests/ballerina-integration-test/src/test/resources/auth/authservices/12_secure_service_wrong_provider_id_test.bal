import ballerina/http;

http:AuthProvider basicAuthProvider12 = {
    id: "basic1",
    scheme: "basic",
    authStoreProvider: "config"
};

listener http:Listener listener12 = new(9194, config = {
    authProviders: [basicAuthProvider12],
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/echo",
    authConfig: {
        authProviders: ["basic2"],
        authentication: { enabled: true },
        scopes: ["scope2"]
    }
}
service echo12 on listener12 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/test"
    }
    resource function echo(http:Caller caller, http:Request req) {
        _ = caller->respond(());
    }
}