import ballerina/http;
import ballerina/log;

// Header name to be set to the request in the filter.
final string filter_name_header = "X-requestHeader";
// Header value to be set to the request in the filter.
final string filter_name_header_value = "RequestFilter";

// The Request filter implementation. It intercepts the request and adds a new
// header to request before it is dispatched to the HTTP resource.
public type RequestFilter object {
    *http:RequestFilter;
    // Intercepts the request.
    public function filterRequest(http:Caller caller, http:Request request,
                        http:FilterContext context) returns boolean {
        // Set a header to the request inside the filter.
        request.setHeader(filter_name_header, filter_name_header_value);
        // Return true on success.
        return true;
    }
};

// Create a new RequestFilter.
RequestFilter requestFilter = new;

// The response filter implementation. It intercepts the response in response 
// path and adds a new header to response.
public type ResponseFilter object {
    *http:ResponseFilter;
    // Intercepts the response.
    public function filterResponse(http:Response response, 
                        http:FilterContext context) returns boolean {
        // Set a header to the response inside the filter.
        response.setHeader("X-responseHeader", "ResponseFilter");
        // Return true on success.
        return true;
    }
};

// Create a new ResponseFilter.
ResponseFilter responseFilter = new;

// Create an HTTP listener and assign the filters as a config parameter.
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

