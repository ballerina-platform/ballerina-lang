import ballerina.net.http;

@http:configuration {
    allowOrigins :["http://www.m3.com", "http://www.hello.com"],
    allowCredentials : false,
    allowHeaders : ["CORELATION_ID"],
    exposeHeaders : ["Content-Length"],
    maxAge : 84900
}
service<http> crossOriginService {

    @http:resourceConfig {
        methods:["GET"],
        allowOrigins :["http://www.bbc.com"],
        allowCredentials : true,
        allowHeaders: ["X-Content-Type-Options", "X-PINGOTHER"]
    }
    resource companyInfo (http:Request req, http:Response res) {
        json responseJson = {"type":"middleware"};
        res.setJsonPayload(responseJson);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["POST"]
    }
    resource langInfo (http:Request req, http:Response res) {
        json responseJson = {"lang":"Ballerina"};
        res.setJsonPayload(responseJson);
        _ = res.send();
    }
}
