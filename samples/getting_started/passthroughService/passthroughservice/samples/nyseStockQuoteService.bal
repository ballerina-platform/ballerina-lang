package passthroughservice.samples;

import ballerina.net.http;

@http:configuration {basePath:"/nyseStock"}
service<http> nyseStockQuote {

    @http:resourceConfig {
        methods:["GET"]
    }
    resource stocks (http:Connection conn, http:InRequest inReq) {
        http:OutResponse res = {};
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}
