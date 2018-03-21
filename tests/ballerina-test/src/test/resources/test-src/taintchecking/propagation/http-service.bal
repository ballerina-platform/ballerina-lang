import ballerina/net.http;

endpoint http:ServiceEndpoint helloWorldEP {
port:9090
};

service<http:Service> sample bind helloWorldEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    params (endpoint outboundEP, http:Request req, string foo) {
        map paramsMap = req.getQueryParams();
        var bar, _ = (string) paramsMap.bar;

        secureFunction(foo, foo);
        secureFunction(bar, bar);
    }
}


public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

