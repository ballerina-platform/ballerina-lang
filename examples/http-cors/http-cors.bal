import ballerina/http;

endpoint http:ServiceEndpoint crossOriginServiceEP {
    port:9092
};

@Description {value:"Service level CORS headers applies globally for each resource"}
@http:ServiceConfig {
    cors: {
        allowOrigins :["http://www.m3.com", "http://www.hello.com"],
        allowCredentials : false,
        allowHeaders:["CORELATION_ID"],
        exposeHeaders : ["X-CUSTOM-HEADER"],
        maxAge : 84900
    }
}
service<http:Service> crossOriginService bind crossOriginServiceEP {

    @Description {value:"CORS headers are defined at resource level are overrides the service level headers"}
    @http:ResourceConfig {
        methods:["GET"],
        path:"/company",
        cors : {
            allowOrigins :["http://www.bbc.com"],
            allowCredentials : true,
            allowHeaders: ["X-Content-Type-Options", "X-PINGOTHER"]
        }
    }
   companyInfo (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"type":"middleware"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @Description {value:"Service level CORS headers are applies to this resource as cors are not defined at resource level"}
    @http:ResourceConfig {
        methods:["POST"],
        path:"/lang"
    }
    langInfo (endpoint conn, http:Request req) {
        http:Response res = new;
        json responseJson = {"lang":"Ballerina"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}
