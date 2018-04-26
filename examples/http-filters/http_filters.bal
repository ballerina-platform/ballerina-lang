import ballerina/http;

@final string filter_name_header = "filterName";
@final string filter_name_header_value = "RequestFilter-1";

public type RequestFilter1 object {
    public function filterRequest(http:Request request, http:FilterContext
    context) returns http:FilterResult {
        // set a header for filter
        request.setHeader(filter_name_header, filter_name_header_value);
        http:FilterResult filterResponse = { canProceed: true, statusCode: 200,
            message: "successful" };
        return filterResponse;
    }
};

RequestFilter1 filter;

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
        _ = caller->respond(res);
    }
}

