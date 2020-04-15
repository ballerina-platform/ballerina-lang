import ballerina/http;

listener http:MockListener testEP = new(9090);

@http:ServiceConfig {
    basePath:"/identifierLiteral",
    cors: {
              allowOrigins :["http://www.m3.com", "http://www.hello.com"],
              allowCredentials : true,
              allowHeaders :["CORELATION_ID"],
              exposeHeaders :["CORELATION_ID"],
              maxAge : 1
          }
}
service 'sample\ Service on testEP{

    @http:ResourceConfig {
        methods:["GET"],
        path:"/resource"
    }
    resource function 'sample\ resource (http:Caller caller, http:Request req) {
        http:Response res = new;
        json responseJson = {"key":"keyVal", "value":"valueOfTheString"};
        res.setJsonPayload(responseJson);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/resource2"
    }
    resource function 'sample\ resource2 (http:Caller caller, http:Request req) {
        http:Response res = new;
        string 'a\ a = "hello";
        res.setTextPayload('a\ a);
        checkpanic caller->respond(res);
    }
}

