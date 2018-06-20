import ballerina/http;
import ballerina/log;

@final string filter_name_header = "X-filterName";
@final string filter_name_header_value = "RequestFilter-1";

public type RequestFilter object {
    public function filterRequest(http:Listener listener, http:Request request, http:FilterContext context)
                        returns boolean {
        // set a header for filter
        request.setHeader(filter_name_header, filter_name_header_value);
        return true;
    }

    public function filterResponse(http:Response response, http:FilterContext context) returns boolean {
        return true;
    }
};

RequestFilter filter;

endpoint http:Listener echoEP {
    port: 9090,
    filters: [filter]
};

@http:ServiceConfig {
    basePath: "/hello"
}
service<http:Service> echo bind echoEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/sayHello"
    }
    echo(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setHeader(filter_name_header, req.getHeader(filter_name_header));
        res.setPayload("Hello, World!");
        caller->respond(res) but {error e => log:printError("Error sending response", err = e)};
    }
}

