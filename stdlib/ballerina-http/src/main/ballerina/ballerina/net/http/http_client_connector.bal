package ballerina.net.http;

@Description { value:"HTTP client connector for outbound HTTP requests"}
public struct HttpClient {
    //These properties are populated from the init call to the client connector as these were needed later stage
    //for retry and other few places.
    string serviceUri;
    ClientEndpointConfiguration config;
}

public function <HttpClient conn> HttpClient() {

}

@Description {value:"The POST action implementation of the HTTP Connector."}
@Param {value:"path: Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public native function <HttpClient client> post(string path, Request req) (Response, HttpConnectorError);

@Description {value:"The HEAD action implementation of the HTTP Connector."}
@Param {value:"path: Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public native function <HttpClient client> head(string path, Request req) (Response, HttpConnectorError);

@Description {value:"The PUT action implementation of the HTTP Connector."}
@Param {value:"path: Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public native function <HttpClient client> put(string path, Request req) (Response, HttpConnectorError);

@Description {value:"Invokes an HTTP call with the specified HTTP verb."}
@Param {value:"httpVerb: HTTP verb value"}
@Param {value:"path: Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public native function <HttpClient client> execute(string httpVerb, string path, Request req) (Response, HttpConnectorError);

@Description {value:"The PATCH action implementation of the HTTP Connector."}
@Param {value:"path: Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public native function <HttpClient client> patch(string path, Request req) (Response, HttpConnectorError);

@Description {value:"The DELETE action implementation of the HTTP connector"}
@Param {value:"path: Resource path "}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public native function <HttpClient client> delete(string path, Request req) (Response, HttpConnectorError);

@Description {value:"GET action implementation of the HTTP Connector"}
@Param {value:"path: Request path"}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public native function <HttpClient client> get(string path, Request req) (Response, HttpConnectorError);

@Description {value:"OPTIONS action implementation of the HTTP Connector"}
@Param {value:"path: Request path"}
@Param {value:"req: An HTTP outbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public native function <HttpClient client> options(string path, Request req) (Response, HttpConnectorError);

@Description {value:"Forward action can be used to invoke an HTTP call with inbound request's HTTP verb"}
@Param {value:"path: Request path"}
@Param {value:"req: An HTTP inbound request message"}
@Return {value:"The inbound response message"}
@Return {value:"Error occured during HTTP client invocation"}
public native function <HttpClient client> forward(string path, Request req) (Response, HttpConnectorError);

@Description { value:"Submits an HTTP request to a service with the specified HTTP verb."}
@Param { value:"httpVerb: The HTTP verb value" }
@Param { value:"path: The Resource path " }
@Param { value:"req: An HTTP outbound request message" }
@Return { value:"The Handle for further interactions" }
@Return { value:"The Error occured during HTTP client invocation" }
public native function <HttpClient client> submit(string httpVerb, string path, Request req) (HttpHandle, HttpConnectorError);

@Description { value:"Retrieves response for a previously submitted request."}
@Param { value:"handle: The Handle which relates to previous async invocation" }
@Return { value:"The HTTP response message" }
@Return { value:"The Error occured during HTTP client invocation" }
public native function <HttpClient client> getResponse(HttpHandle handle) (Response, HttpConnectorError);

@Description { value:"Checks whether server push exists for a previously submitted request."}
@Param { value:"handle: The Handle which relates to previous async invocation" }
@Return { value:"Whether push promise exists" }
public native function <HttpClient client> hasPromise(HttpHandle handle) (boolean);

@Description { value:"Retrieves the next available push promise for a previously submitted request."}
@Param { value:"handle: The Handle which relates to previous async invocation" }
@Return { value:"The HTTP Push Promise message" }
@Return { value:"The Error occured during HTTP client invocation" }
public native function <HttpClient client> getNextPromise(HttpHandle handle) (PushPromise, HttpConnectorError);

@Description { value:"Retrieves the promised server push response."}
@Param { value:"promise: The related Push Promise message" }
@Return { value:"HTTP The Push Response message" }
@Return { value:"The Error occured during HTTP client invocation" }
public native function <HttpClient client> getPromisedResponse(PushPromise promise) (Response, HttpConnectorError);

@Description { value:"Rejects a push promise."}
@Param { value:"promise: The Push Promise need to be rejected" }
@Return { value:"Whether operation is successful" }
public native function <HttpClient client> rejectPromise(PushPromise promise) (boolean);

@Description { value:"HttpConnectorError struct represents an error occured during the HTTP client invocation" }
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error(s) that caused HttpConnectorError to get thrown"}
@Field {value:"statusCode: HTTP status code"}
public struct HttpConnectorError {
    string message;
    error[] cause;
    int statusCode;
}

@Description { value:"ClientEndpointConfiguration struct represents options to be used for HTTP client invocation" }
@Field {value:"endpointTimeout: Endpoint timeout value in millisecond"}
@Field {value:"keepAlive: Specifies whether to reuse a connection for multiple requests"}
@Field {value:"transferEncoding: The types of encoding applied to the request"}
@Field {value:"chunking: The chunking behaviour of the request"}
@Field {value:"httpVersion: The HTTP version understood by the client"}
@Field {value:"forwarded: The choice of setting forwarded/x-forwarded header"}
@Field {value:"followRedirects: Redirect related options"}
@Field {value:"retryConfig: Retry related options"}
@Field {value:"proxy: Proxy server related options"}
@Field {value:"connectionThrottling: Configurations for connection throttling"}
@Field {value:"targets: Service(s) accessible through the endpoint. Multiple services can be specified here when using techniques such as load balancing and fail over."}
public struct ClientEndpointConfiguration {
    int endpointTimeout = 60000;
    boolean keepAlive = true;
    TransferEncoding transferEncoding;
    Chunking chunking;
    string httpVersion;
    string forwarded = "disable";
    FollowRedirects followRedirects;
    Retry retryConfig;
    Proxy proxy;
    ConnectionThrottling connectionThrottling;
    TargetService[] targets;
    function (LoadBalancer, HttpClient[])(HttpClient) algorithm;
}
