package passthroughservice.samples;

import ballerina.net.http;
import ballerina.net.http.response;

@http:configuration {basePath:"/passthrough"}
service<http> passthrough {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource passthrough (http:Request req, http:Response res) {
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090");
        res = nyseEP.get("/nyseStock/stocks", req);
        response:send(res);
    }
}
