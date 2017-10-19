import ballerina.net.http;
import ballerina.doc;

service<http> sample {

    @http:resourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    @doc:Description {value:"PathParam and QueryParam extract values from the request URI."}
    resource params (http:Request req, http:Response res, string foo) {
        // Get QueryParam
        map params = req.getQueryParams();
        var bar, _ = (string)params.bar;
        // Create json payload with the extracted values.
        json responseJson = {"queryParam":foo, "pathParam":bar};
        // A util method to set the json payload to the response message.
        res.setJsonPayload(responseJson);
        // Send a response to the client.
        res.send();
    }
}
