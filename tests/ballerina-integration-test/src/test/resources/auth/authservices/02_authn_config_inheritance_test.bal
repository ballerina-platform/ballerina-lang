import ballerina/http;
import ballerina/io;
import ballerina/auth;

http:AuthProvider basicAuthProvider02 = {
    scheme:"basic",
    authStoreProvider:"config"
};

listener http:Listener listener02 = new(9091, config = {
    authProviders:[basicAuthProvider02],
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
        authentication:{enabled:false},
        scopes:["xxx", "aaa"]
    }
}

service echo02 on listener02 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test",
        authConfig:{
            authentication:{enabled:true}
        }
    }
    resource function echo (http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}
