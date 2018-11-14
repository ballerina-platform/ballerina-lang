import ballerina/http;

endpoint http:Listener headerServiceEP {
    port: 9106
};

endpoint http:Listener stockServiceEP {
    port: 9107
};

endpoint http:Client stockqEP {
    url: "http://localhost:9107"
};

@http:ServiceConfig {
    basePath:"/product"
}
service<http:Service> headerService bind headerServiceEP {

    value (endpoint conn, http:Request req) {
        req.setHeader("core", "aaa");
        req.addHeader("core", "bbb");

        var result = stockqEP -> get("/sample/stocks", message = untaint req);
        if (result is http:Response) {
            _ = conn->respond(result);
        } else if (result is error) {
            _ = conn->respond(result.reason());
        }
    }

    id (endpoint conn, http:Request req) {
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
            _ = conn -> respond(payload);
        } else if (clientResponse is error) {
            _ = conn -> respond(clientResponse.reason());
        }
    }
}

@http:ServiceConfig {
    basePath:"/sample"
}
service<http:Service> quoteService1 bind stockServiceEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    company (endpoint conn, http:Request req) {
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
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    product (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setHeader("person", "kkk");
        res.addHeader("person", "jjj");
        _ = conn -> respond(res);
    }
}
