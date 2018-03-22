import ballerina/net.http;
import ballerina/net.http.mock;

endpoint mock:NonListeningService testEP {
    port:9090
};


@http:ServiceConfig {
    basePath:"/identifierLiteral",
    endpoints:[testEP],
    cors: {
              allowOrigins :["http://www.m3.com", "http://www.hello.com"],
              allowCredentials : true,
              allowHeaders :["CORELATION_ID"],
              exposeHeaders :["CORELATION_ID"],
              maxAge : 1
          }
}

service<http:Service> ^"sample Service" {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/resource"
    }
    ^"sample resource" (endpoint outboundEp, http:Request req) {
        http:Response res = {};
        json responseJson = {"key":"keyVal", "value":"valueOfTheString"};
        res.setJsonPayload(responseJson);
        _ = outboundEp -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/resource2"
    }
    ^"sample resource2" (endpoint outboundEp, http:Request req) {
        http:Response res = {};
        string ^"a a" = "hello";
        res.setStringPayload(^"a a");
        _ = outboundEp -> respond(res);
    }
}

