import ballerina/http;

http:AuthProvider basicAuthProvider03 = {
    scheme: "basic",
    authStoreProvider: "config"
};

listener http:Listener listener03 = new(9092, config = {
    authProviders: [basicAuthProvider03],
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
        authentication: { enabled: true },
        scopes: ["xxx"]
    }
}
service echo03 on listener03 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/test",
        authConfig: {
            scopes: ["scope2", "scope4"]
        }
    }
    resource function echo(http:Caller caller, http:Request req) {
        _ = caller->respond(());
    }
}
