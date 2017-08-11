import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.doc;

@http:configuration {basePath:"/cbr"}
service<http> contentBasedRouting {
    @doc:Description {value:"http:POST{} annotation declares the HTTP method."}
    @http:POST {}
    @http:Path {value:"/route"}
    resource cbrResource (message m) {

        //Create service endpoint using HTTP client-connector.
        http:ClientConnector locationEP = create http:ClientConnector(
                                          "http://www.mocky.io");
        //Get JSON payload from the request message.
        json jsonMsg = messages:getJsonPayload(m);
        //Get the string value relevant to the key "name".
        string nameString;
        nameString, _ = (string)jsonMsg["name"];

        //Additionally HTTP HEAD request can be executed to verify the accessibility.
        locationEP.head("/v2/594e018c1100002811d6d39a", m);

        message response = {};
        if (nameString == "sanFrancisco") {
        //"post" represent the POST action of HTTP connector. Route payload to relevant service as the server accept the entity enclosed.
            response = locationEP.post("/v2/594e018c1100002811d6d39a", m);
            //Additionally, If the payload needed to be stored under requested resource path, PUT action will help as follows.
            locationEP.put("/v2/594e018c1100002811d6d39a", m);

        } else {
            response = locationEP.post("/v2/594e026c1100004011d6d39c", m);
            //Requested resources can be deleted using DELETE action.
            locationEP.delete("/v2/594e026c1100004011d6d39c", m);
        }

        reply response;
    }
}

