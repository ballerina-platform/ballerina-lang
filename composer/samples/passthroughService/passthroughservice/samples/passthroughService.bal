package passthroughservice.samples;

import ballerina.net.http;

@http:configuration {basePath:"/passthrough"}
service<http> passthrough {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource passthrough (http:Connection conn, http:InRequest inRequest) {
        endpoint<http:HttpClient> nyseEP {
            create http:HttpClient("http://localhost:9090", {});
        }
        http:OutRequest clientRequest = {};
        var clientResponse, _ = nyseEP.get("/nyseStock/stocks", clientRequest);
        _ = conn.forward(clientResponse);
    }
}
