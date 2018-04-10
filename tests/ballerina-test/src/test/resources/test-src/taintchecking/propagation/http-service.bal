import ballerina/http;

endpoint http:Listener helloWorldEP {
port:9090
};

service<http:Service> sample bind helloWorldEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    params (endpoint outboundEP, http:Request req, string foo) {
        map paramsMap = req.getQueryParams();
        var bar = paramsMap.bar;

        secureFunction(foo, foo);
        secureFunction(bar, bar);
    }
}


public function secureFunction (@sensitive any secureIn, any insecureIn) {

}

