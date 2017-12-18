package passthroughservice.samples;

import ballerina.net.http;

@http:configuration {basePath:"/passthrough"}
service<http> passthrough {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource passthrough (http:Connection con, http:Request req) {
        endpoint<http:HttpClient> nyseEP {
            create http:HttpClient("http://localhost:9090", {});
        }
        http:Response clientResponse = {};
        http:HttpConnectorError err;
        clientResponse, err = nyseEP.get("/nyseStock/stocks", req);
        _ = con.respond(clientResponse);
    }
}
