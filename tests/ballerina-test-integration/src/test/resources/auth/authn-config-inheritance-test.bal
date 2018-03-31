import ballerina/http;
import ballerina/io;
import ballerina/auth;

endpoint http:ApiEndpoint ep {
    port:9090
};

@http:ServiceConfig {
      basePath:"/echo"
}

@auth:Config {
    authentication:{enabled:false},
    scopes:["xxx", "aaa"]
}   
service<http:Service> echo bind ep {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test"
    }
    @auth:Config {
        authentication:{enabled:true}
    }
    echo (endpoint client, http:Request req) {
        http:Response res = {};
        _ = client -> respond(res);
    }
}
