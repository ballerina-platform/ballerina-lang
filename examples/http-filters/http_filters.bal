import ballerina/http;
import ballerina/log;

final string filter_name_header = "X-filterName";
final string filter_name_header_value = "RequestFilter-1";

public type RequestFilter object {
    public function filterRequest(http:Caller caller, http:Request request,
                        http:FilterContext context)
                        returns boolean {
        // set a header for filter
        request.setHeader(filter_name_header, filter_name_header_value);
        return true;
    }

    public function filterResponse(http:Response response,
                                   http:FilterContext context)
                                    returns boolean {
        return true;
    }
};

RequestFilter filter = new;

listener http:Listener echoListener = new http:Listener(9090,
                                            config = { filters: [filter]});

@http:ServiceConfig {
    basePath: "/hello"
}
service echo on echoListener {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/sayHello"
    }
    resource function echo(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setHeader(filter_name_header, req.getHeader(filter_name_header));
        res.setPayload("Hello, World!");
        var result = caller->respond(res);
        if (result is error) {
           log:printError("Error sending response", err = result);
        }
    }
}

