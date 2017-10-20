package passthroughservice.samples;

import ballerina.net.http;

@http:configuration {basePath:"/passthrough"}
service<http> passthrough {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource passthrough (http:Request req, http:Response resp) {
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090", {});
        http:Response clientResponse = nyseEP.get("/nyseStock/stocks", req);
        resp.forward(clientResponse);
    }
}
