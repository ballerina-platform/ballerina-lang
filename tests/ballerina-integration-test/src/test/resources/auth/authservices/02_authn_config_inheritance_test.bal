import ballerina/http;
import ballerina/io;
import ballerina/auth;

http:AuthProvider basicAuthProvider02 = {
    scheme:"basic",
    authStoreProvider:"config"
};

endpoint http:Listener listener02 {
    port:9091,
    authProviders:[basicAuthProvider02],
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
        authentication:{enabled:false},
        scopes:["xxx", "aaa"]
    }
}

service<http:Service> echo02 bind listener02 {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test",
        authConfig:{
            authentication:{enabled:true}
        }
    }
    echo (endpoint caller, http:Request req) {
        http:Response res = new;
        _ = caller -> respond(res);
    }
}
