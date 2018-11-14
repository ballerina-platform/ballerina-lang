import ballerina/http;
import ballerina/auth;

http:AuthProvider basicAuthProvider02 = {
    id: "basic1",
    scheme: "basic",
    authStoreProvider: "config"
};

endpoint http:SecureListener listener02 {
    port:9093,
    authProviders:[basicAuthProvider02]
};

@http:ServiceConfig {
    basePath: "/echo",
    authConfig: {
        authProviders: ["basic1"],
        authentication: { enabled: true },
        scopes: ["scope2"]
    }
}
service<http:Service> echo02 bind listener02 {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/test"
    }
    echo(endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller->respond(res);
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
