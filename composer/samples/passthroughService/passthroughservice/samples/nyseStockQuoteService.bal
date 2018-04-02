package passthroughservice.samples;

import ballerina/http;

@http:configuration {basePath:"/nyseStock"}
service<http> nyseStockQuote {

    @http:resourceConfig {
        methods:["GET"]
    }
    resource stocks (http:Connection conn, http:Request inReq) {
        http:Response res = {};
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}
