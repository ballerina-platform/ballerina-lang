import ballerina/http;
import ballerina/lang.messages;
import ballerina/doc;

service<http> sample {

    @http:GET {}
    @http:Path {value:"/path/{foo}"}
    @doc:Description {value:"PathParam and QueryParam extract values from the request URI."}
    resource params (message m, @http:PathParam {value:"foo"} string foo,
                                @http:QueryParam {value:"bar"} string bar) {
        // Create a response message
        message response = {};
        // Create json payload with the extracted values.
        json responseJson = {"queryParam":foo, "pathParam":bar};
        // A util method to set the json payload to the response message.
        messages:setJsonPayload(response, responseJson);
        // Send a response to the client.
        reply response;
    }
}
