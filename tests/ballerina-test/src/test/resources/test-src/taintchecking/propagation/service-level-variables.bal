import ballerina.net.http;

endpoint http:ServiceEndpoint helloWorldEP {
port:9090
};

string globalLevelVariable = "";
service<http:Service> sample bind helloWorldEP {
    string serviceLevelVariable = "";

    @http:ResourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    params (endpoint outboundEP, http:Request req, string foo) {
        map paramsMap = req.getQueryParams();
        var bar, _ = (string) paramsMap.bar;

        serviceLevelVariable = "static";
        globalLevelVariable = "static";
    }
}

