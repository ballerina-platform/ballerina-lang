import ballerina/mime;
import ballerina/http;

@Description {value:"Define the attributes associated with the service endpoint here."}
endpoint http:Listener cbrEP {
    port:9090
};

@Description {value:"Define the attributes associated with the client endpoint here."}
endpoint http:Client locationEP {
    targets:[{url: "http://www.mocky.io"}]
};


@http:ServiceConfig { basePath:"/cbr" }
service<http:Service> contentBasedRouting bind cbrEP {
    @Description {value:"Use the @http:POST{} annotation to declare the HTTP method."}
    @http:ResourceConfig {
        methods:["POST"],
        path:"/route"
    }
    cbrResource (endpoint outboundEP, http:Request req) {
        //Get the JSON payload from the request message.
        var jsonMsg = req.getJsonPayload();

        match jsonMsg {
            json msg => {
                //Get the string value relevant to the key 'name'.
                string nameString;
                nameString = check <string>msg["name"];
                (http:Response|http:HttpConnectorError|()) clientResponse;

                if (nameString == "sanFrancisco") {
                    //Here, 'post' represents the POST action of the HTTP client connector.
                    //This routes the payload to the relevant service when the server accepts the enclosed entity.
                    clientResponse = locationEP -> post("/v2/594e018c1100002811d6d39a", new);
                } else {
                    clientResponse = locationEP -> post("/v2/594e026c1100004011d6d39c", new);
                }
                //Use the native function 'respond' to send the client response back to the caller.
                match clientResponse {
                    http:Response respone => {
                        _ = outboundEP -> respond(respone);
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
