import ballerina.net.http;
import ballerina.doc;

@http:configuration {basePath:"/cbr"}
service<http> contentBasedRouting {
    @doc:Description {value:"http:POST{} annotation declares the HTTP method."}
    @http:resourceConfig {
        methods:["POST"],
        path:"/route"
    }
    resource cbrResource (http:Request req, http:Response res) {
        http:ClientConnector locationEP;
        //Create service endpoint using HTTP client-connector.
        locationEP = create http:ClientConnector("http://www.mocky.io", {});
        //Get JSON payload from the request message.
        json jsonMsg = req.getJsonPayload();
        //Get the string value relevant to the key "name".
        string nameString;
        nameString, _ = (string)jsonMsg["name"];
        http:Response clientResponse = {};
        if (nameString == "sanFrancisco") {
            //"post" represent the POST action of HTTP connector. Route payload to relevant service as the server accept the entity enclosed.
            clientResponse = locationEP.post("/v2/594e018c1100002811d6d39a", {});
        } else {
            clientResponse = locationEP.post("/v2/594e026c1100004011d6d39c", {});
        }
        //Native function "forward" sends back the clientResponse to the caller.
        res.forward(clientResponse);
    }
}
