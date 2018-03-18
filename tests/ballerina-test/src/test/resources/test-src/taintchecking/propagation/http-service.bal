import ballerina.net.http;

service<http> sample {
    @http:resourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    resource params (http:Connection conn, http:Request req, string foo) {
        map params = req.getQueryParams();
        var bar, _ = (string) params.bar;

        secureFunction(foo, foo);
        secureFunction(bar, bar);
    }
}


public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
