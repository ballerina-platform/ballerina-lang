
import ballerina/http;

@http:configuration {basePath:"/nyseStocks"}
service<http> nyseStockQuote {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource stocks (http:Connection con, http:Request req) {
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        http:Response res = {};
        res.setJsonPayload(payload);
        _ = con.respond(res);
    }
}
