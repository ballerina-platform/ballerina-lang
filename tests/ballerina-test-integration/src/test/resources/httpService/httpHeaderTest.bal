import ballerina/http;

endpoint http:Listener headerServiceEP {
    port: 9090
};

endpoint http:Listener stockServiceEP {
    port: 9091
};

endpoint http:Client stockqEP {
    targets:[{url: "http://localhost:9091"}]
};

@http:ServiceConfig {
    basePath:"/product"
}
service<http:Service> headerService bind headerServiceEP {

    value (endpoint conn, http:Request req) {
        req.setHeader("core", "aaa");
        req.addHeader("core", "bbb");

        var result = stockqEP -> get("/sample/stocks", req);
        match result {
            http:Response clientResponse => {
                _ = conn -> respond(clientResponse);
            }
            any|() => {}
        }
    }

    id (endpoint conn, http:Request req) {
        http:Response clntResponse = new;
        var reply = stockqEP -> forward("/sample/customers", req);

        match reply {
            http:Response clientResponse => {
                json payload = {};
                if (clientResponse.hasHeader("person")) {
                    string[] headers = clientResponse.getHeaders("person");
                    if (lengthof(headers) == 2) {
                        payload = {header1:headers[0], header2:headers[1]};
                    } else {
                        payload = {"response":"expected number of 'person' headers not found"};
                    }
                } else {
                    payload = {"response":"person header not available"};
                }
                http:Response res = new;
                res.setJsonPayload(payload);
                _ = conn -> respond(res);
            }
            any|() => {}
        }
    }
}

@http:ServiceConfig {
    basePath:"/sample"
}
service<http:Service> quoteService bind stockServiceEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    company (endpoint conn, http:Request req) {
        json payload = {};
        if (req.hasHeader("core")) {
            string[] headers = req.getHeaders("core");
            if (lengthof(headers) == 2) {
                payload = {header1:headers[0], header2:headers[1]};
            } else {
                payload = {"response":"expected number of 'core' headers not found"};
            }
        } else {
            payload = {"response":"core header not available"};
        }
        http:Response res = new;
        res.setJsonPayload(payload);
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
