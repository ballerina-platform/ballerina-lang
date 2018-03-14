import ballerina.net.http;

endpoint<http:Service> crossOriginServiceEP {
    port:9092
}

@Description {value:"Service level CORS headers applies globally for each resource"}
@http:serviceConfig {
    endpoints:[crossOriginServiceEP],
    allowOrigins :["http://www.m3.com", "http://www.hello.com"],
    allowCredentials : false,
    allowHeaders : ["CORELATION_ID"],
    exposeHeaders : ["X-CUSTOM-HEADER"],
    maxAge : 84900
}
service<http:Service> crossOriginService {

    @Description {value:"CORS headers are defined at resource level are overrides the service level headers"}
    @http:resourceConfig {
        methods:["GET"],
        path:"/company",
        allowOrigins :["http://www.bbc.com"],
        allowCredentials : true,
        allowHeaders: ["X-Content-Type-Options", "X-PINGOTHER"]
    }
    resource companyInfo (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"type":"middleware"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }

    @Description {value:"Service level CORS headers are applies to this resource as cors are not defined at resource level"}
    @http:resourceConfig {
        methods:["POST"],
        path:"/lang"
    }
    resource langInfo (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        json responseJson = {"lang":"Ballerina"};
        res.setJsonPayload(responseJson);
        _ = conn -> respond(res);
    }
}
