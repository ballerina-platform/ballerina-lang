import ballerina/jballerina.java;

# The HTTP client provides the capability for initiating contact with a remote HTTP service. The API it
# provides includes functions for the standard HTTP methods, forwarding a received request and sending requests
# using custom HTTP verbs.

# + url - Target service url
public client class Client {
    public string url;

    public function init(string url) {
        self.url = url;
    }

    # The `Client.post()` function can be used to send HTTP POST requests to HTTP endpoints.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`
    # + targetType - Specifies the target type
    # + return - The response for the request
    remote function post(@untainted string path, RequestMessage message, string targetType="xml") returns Response {
        Response response = new();
        return response;
    }
    
    # Sample resource method.
    #
    # + id1 - Path parameter
    # + ids - Rest path parameter
    # + str - Argument
    # + ids2 - Rest argument
    # + return - The response for the request
    resource function post path1/[string id1]/path2/[string... ids](string str, string... ids2) returns Response {
        return new Response();
    }
    
    # Sample remote method with java interoperability
    #
    # + path - Resource path
    # + targetType - Response or `anydata`, which is expected to be returned after data binding
    # + return - The Response or the ClientError
    remote function get(string path, TargetType targetType = <>) returns targetType|ClientError = @java:Method {
        'class: "org.ballerinalang.langserver.stdlib.ClientAction"
    } external;
    
    # Sample remote method with java interoperability
    #
    # + path - Resource path
    # + request - Request need to be forward
    # + targetType - Response or `anydata`, which is expected to be returned after data binding
    # + return - Response
    remote function forward(string path, handle request, TargetType targetType = <>) returns targetType = @java:Method {
        'class: "org.ballerinalang.langserver.stdlib.ClientAction"
    } external;
    
    # Sample remote method with java interoperability
    #
    # + targetType - `any`type which is expected to be returned after data binding
    # + return - Type any
    remote function delete(TargetType2 targetType = <>) returns targetType = @java:Method {
        'class: "org.ballerinalang.langserver.stdlib.ClientAction"
    } external;
    
    # Sample resource function with java interoperability
    #
    # + targetType - Response or `anydata`, which is expected to be returned after data binding
    # + return - The Response or the ClientError
    resource function get path3(TargetType targetType = <>) returns targetType|ClientError = @java:Method {
        'class: "org.ballerinalang.langserver.stdlib.ClientAction",
        name: "delete"
    } external;
    
    # Sample resource function with multiple target types with java interoperability
    #
    # + targetType - Response or `anydata`, which is expected to be returned after data binding
    # + return - The Response or the ClientError
    resource function get path4/[string pathParam](TargetType targetType = <>) returns targetType|json|ClientError|error = @java:Method {
        'class: "org.ballerinalang.langserver.stdlib.ClientAction",
        name: "get"
    } external;
}

# Represents a response.
#
# + statusCode - The response status code
# + reasonPhrase - The status code reason phrase
public class Response {
    public int statusCode = 200;
    public string reasonPhrase = "Test Reason phrase";
}

# The types of messages that are accepted by HTTP `listener` when sending out the outbound response.
public type ResponseMessage string|xml|json|byte[]|();

# The types of messages that are accepted by HTTP `client` when sending out the outbound request.
public type RequestMessage string|xml|json|byte[]|();

# The types of data values that are expected by the `client` to return after the data binding operation.
public type TargetType typedesc<Response|anydata>;

# Defines the possible client error types.
public type ClientError distinct error<map<anydata>>;

# The super type of all the types.
public type TargetType2 typedesc<any>;
