import ballerina.net.http;
import ballerina.doc;
import ballerina.net.http.request;
import ballerina.net.http.response;

service<http> sample {

    @http:resourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    @doc:Description {value:"PathParam and QueryParam extract values from the request URI."}
    resource params (http:Request req, http:Response res, string foo) {
        // Get QueryParam
        map params = request:getQueryParams(req);
        var bar, _ = (string)params.bar;
        // Create json payload with the extracted values.
        json responseJson = {"queryParam":foo, "pathParam":bar};
        // A util method to set the json payload to the response message.
        response:setJsonPayload(res, responseJson);
        // Send a response to the client.
        response:send(res);
    }
}
