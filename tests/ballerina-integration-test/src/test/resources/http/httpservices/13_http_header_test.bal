import ballerina/http;

http:Client stockqEP = new("http://localhost:9107");

@http:ServiceConfig {
    basePath:"/product"
}
service headerService on new http:Listener(9106) {

    resource function value(http:Caller caller, http:Request req) {
        req.setHeader("core", "aaa");
        req.addHeader("core", "bbb");

        var result = stockqEP -> get("/sample/stocks", message = untaint req);
        if (result is http:Response) {
            _ = caller->respond(result);
        } else if (result is error) {
            _ = caller->respond(result.reason());
        }
    }

    resource function id(http:Caller caller, http:Request req) {
        http:Response clntResponse = new;
        var clientResponse = stockqEP -> forward("/sample/customers", req);
        if (clientResponse is http:Response) {
            json payload = {};
            if (clientResponse.hasHeader("person")) {
                string[] headers = clientResponse.getHeaders("person");
                if (headers.length() == 2) {
                    payload = {header1:headers[0], header2:headers[1]};
                } else {
                    payload = {"response":"expected number of 'person' headers not found"};
                }
            } else {
                payload = {"response":"person header not available"};
            }
            _ = caller->respond(payload);
        } else if (clientResponse is error) {
            _ = caller->respond(clientResponse.reason());
        }
    }
}

@http:ServiceConfig {
    basePath:"/sample"
}
service quoteService1 on new http:Listener(9107) {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    resource function company(http:Caller caller, http:Request req) {
        json payload = {};
        if (req.hasHeader("core")) {
            string[] headers = req.getHeaders("core");
            if (headers.length() == 2) {
                payload = {header1:headers[0], header2:headers[1]};
            } else {
                payload = {"response":"expected number of 'core' headers not found"};
            }
        } else {
            payload = {"response":"core header not available"};
        }
        http:Response res = new;
        res.setJsonPayload(untaint payload);
        _ = caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource function product(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setHeader("person", "kkk");
        res.addHeader("person", "jjj");
        _ = caller->respond(res);
    }
}
