import ballerina/http;
import ballerina/log;

// Header name to be set to the request in the filter.
final string filter_name_header = "X-requestHeader";
// Header value to be set to the request in the filter.
final string filter_name_header_value = "RequestFilter";

// The [Request](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/classes/Request.html) implementation.
// It intercepts the request and adds a new header to the request before it is dispatched to the HTTP resource.
public class RequestFilter {
    *http:RequestFilter;
    // [Intercepts the request](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/classes/Request.html#filterRequest).
    public isolated function filterRequest(http:Caller caller,
                        http:Request request, http:FilterContext context)
                        returns boolean {
        // Set a header to the request inside the filter.
        request.setHeader(filter_name_header, filter_name_header_value);
        // Return true on success.
        return true;
    }
}

// Creates a new RequestFilter.
RequestFilter requestFilter = new;

// The [response(https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/classes/Response.html) implementation.
// It intercepts the response in the response path and adds a new header to the response.
public class ResponseFilter {
    *http:ResponseFilter;
    // [Intercepts the response](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/classes/Response.html#filterResponse).
    public isolated function filterResponse(http:Response response, 
                        http:FilterContext context) returns boolean {
        // Sets a header to the response inside the filter.
        response.setHeader("X-responseHeader", "ResponseFilter");
        // Return true on success.
        return true;
    }
}

// Creates a new [Response](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/classes/Response.html).
ResponseFilter responseFilter = new;

// Creates an HTTP listener and assigns the [filters as a config parameter](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/records/ListenerConfiguration.html).
listener http:Listener echoListener = new http:Listener(9090,
                    config = {filters: [requestFilter, responseFilter]});

@http:ServiceConfig {
    basePath: "/hello"
}
service echo on echoListener {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/sayHello"
    }
    resource function echo(http:Caller caller, http:Request req) {
        // Create a new http response.
        http:Response res = new;
        // Set the `filter_name_header` from the request to the response.
        res.setHeader(filter_name_header, req.getHeader(filter_name_header));
        res.setPayload("Hello, World!");
        var result = caller->respond(<@untainted>res);
        if (result is error) {
            log:printError("Error sending response", result);
        }
    }
}

