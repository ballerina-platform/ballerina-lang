import ballerina. net.http;
import ballerina.lang. messages;

@http:config {basePath:"/sample"}
service<http> sample {

    @http:GET {}
    @http:Path {value:"/path/{foo}"}
    @doc:description{value : "PathParam and QueryParam extract values from the request URI."}
    resource params (message m, @http:PathParam {value:"foo"} string foo1, @http:QueryParam {value:"bar"} string bar1) {
        // Create a response message
        message response = {};
        // Create json payload with the extracted values.
        json responseJson = {"queryParam":foo1, "pathParam":bar1};
        // A util method to set the payload to the response message.
        messages:setJsonPayload(response, responseJson);
        // Send back the response to the client.
        reply response;
    }
}
