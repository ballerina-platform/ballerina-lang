import ballerina.net.http;

@Description {value:"Attributes associated with the service endpoint is defined here."}
endpoint<http:Service> cbrEP {
    port:9090
}

@Description {value:"Attributes associated with the client endpoint is defined here."}
endpoint<http:Client> locationEP {
    serviceUri: "http://www.mocky.io"
}


@http:serviceConfig { basePath:"/cbr", endpoints:[cbrEP] }
service<http:Service> contentBasedRouting {
    @Description {value:"http:POST{} annotation declares the HTTP method."}
    @http:resourceConfig {
        methods:["POST"],
        path:"/route"
    }
    resource cbrResource (http:ServerConnector conn, http:Request req) {
        //Get JSON payload from the request message.
        var jsonMsg, payloadError = req.getJsonPayload();
        http:Response res = {};
        if (payloadError == null) {
            //Get the string value relevant to the key "name".
            string nameString;
            nameString, _ = (string)jsonMsg["name"];
            http:Response clientResponse;
            http:HttpConnectorError err;
            if (nameString == "sanFrancisco") {
                //"post" represents the POST action of HTTP client connector. This routes the payload to the relevant service as the server accepts the entity enclosed.
                clientResponse, err = locationEP -> post("/v2/594e018c1100002811d6d39a", {});
            } else {
                clientResponse, err = locationEP -> post("/v2/594e026c1100004011d6d39c", {});
            }
            //Native function "forward" sends back the clientResponse to the caller.
            if (err != null) {
                res.statusCode = 500;
                res.setStringPayload(err.message);
                _ = conn -> respond(res);
            } else {
                _ = conn -> forward(clientResponse);
            }
        } else {
            res.statusCode = 500;
            res.setStringPayload(payloadError.message);
            _ = conn -> respond(res);
        }

    }
}
