import ballerina/http;
import ballerina/io;
import ballerina/auth;

endpoint http:ApiEndpoint ep {
    port:9090
};

@http:ServiceConfig {
    basePath:"/echo"
}

service<http:Service> echo bind ep {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    @auth:Config {
        authentication:{enabled:true},
        scopes:["scope2"]
    }
    echo (endpoint client, http:Request req) {
        http:Response res = {};
        _ = client -> respond(res);
    }
}
