package passthroughservice.samples;

import ballerina.net.http;
import ballerina.net.http.response;

@http:configuration {basePath:"/nyseStock"}
service<http> nyseStockQuote {

    @http:resourceConfig {
        methods:["GET"]
    }
    resource stocks (http:Request req, http:Response res) {
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        response:setJsonPayload(res, payload);
        response:send(res);
    }
}