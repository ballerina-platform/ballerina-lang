import ballerina.net.http;

endpoint<http:Service> helloWorldEP {
port:9090
}

@http:serviceConfig { endpoints:[helloWorldEP] }
service<http:Service> sample {
    @http:resourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    resource params (http:ServerConnector conn, http:Request req, string foo) {
        map params = req.getQueryParams();
        var bar, _ = (string) params.bar;

        secureFunction(foo, foo);
        secureFunction(bar, bar);
    }
}


public function secureFunction (@sensitive{} string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
