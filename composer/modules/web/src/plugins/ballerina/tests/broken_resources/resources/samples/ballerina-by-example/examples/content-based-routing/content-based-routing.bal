import ballerina/http;
import ballerina/lang.messages;
import ballerina/doc;

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
        http:ClientConnector.head(locationEP,
                                  "/v2/594e018c1100002811d6d39a", m);

        message response = {};
        if (nameString == "sanFrancisco") {
        //"post" represent the POST action of HTTP connector. Route payload to relevant service as the server accept the entity enclosed.
            response = http:ClientConnector.post(locationEP,
                                                 "/v2/594e018c1100002811d6d39a", m);
            //Additionally, If the payload needed to be stored under requested resource path, PUT action will help as follows.
            http:ClientConnector.put(locationEP,
                                     "/v2/594e018c1100002811d6d39a", m);

        } else {
            response = http:ClientConnector.post(locationEP,
                                                 "/v2/594e026c1100004011d6d39c", m);
            //Requested resources can be deleted using DELETE action.
            http:ClientConnector.delete(locationEP,
                                        "/v2/594e026c1100004011d6d39c", m);
        }

        reply response;
    }
}

