import ballerina/http;
import ballerina/io;

http:AuthProvider basicAuthProvider01 = {
    scheme:"basic",
    authStoreProvider:"config"
};

listener http:Listener listener01 = new(9090, config = {
    authProviders:[basicAuthProvider01],
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
        scopes:["xxx", "aaa"]
    }
}

service echo01 on listener01 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test",
        authConfig:{
            authentication:{enabled:false}
        }
    }
    resource function echo(http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}

