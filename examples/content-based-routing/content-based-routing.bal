import ballerina/mime;
import ballerina/http;

@Description {value:"Attributes associated with the service endpoint is defined here."}
endpoint http:ServiceEndpoint cbrEP {
    port:9090
};

@Description {value:"Attributes associated with the client endpoint is defined here."}
endpoint http:ClientEndpoint locationEP {
    targets:[{url: "http://www.mocky.io"}]
};


@http:ServiceConfig { basePath:"/cbr" }
service<http:Service> contentBasedRouting bind cbrEP {
    @Description {value:"http:POST{} annotation declares the HTTP method."}
    @http:ResourceConfig {
        methods:["POST"],
        path:"/route"
    }
    cbrResource (endpoint outboundEP, http:Request req) {
        //Get JSON payload from the request message.
        var jsonMsg = req.getJsonPayload();

        match jsonMsg {
            json msg => {
                //Get the string value relevant to the key "name".
                string nameString;
                nameString = check <string>msg["name"];
                (http:Response|http:HttpConnectorError|()) clientResponse;

                if (nameString == "sanFrancisco") {
                    //"post" represents the POST action of HTTP client connector.
                    //This routes the payload to the relevant service as the server accepts the entity enclosed.
                    clientResponse = locationEP -> post("/v2/594e018c1100002811d6d39a", new);
                } else {
                    clientResponse = locationEP -> post("/v2/594e026c1100004011d6d39c", new);
                }
                //Native function "forward" sends back the clientResponse to the caller.
                match clientResponse {
                    http:Response respone => {
                        _ = outboundEP -> forward(respone);
                    }
                    http:HttpConnectorError conError => {
                        http:HttpConnectorError err = {};
                        http:Response res = new;
                        res.statusCode = 500;
                        res.setStringPayload(err.message);
                        _ = outboundEP -> respond(res);
                    }
                    () => {
                    }
                }
            }
            http:PayloadError err => {
                http:Response res = new;
                res.statusCode = 500;
                res.setStringPayload(err.message);
                _ = outboundEP -> respond(res);
            }
        }
    }
}
