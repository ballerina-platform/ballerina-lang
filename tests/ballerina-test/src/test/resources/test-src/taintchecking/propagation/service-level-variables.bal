import ballerina/http;

endpoint http:Listener helloWorldEP {
port:9090
};

any globalLevelVariable = "";
service<http:Service> sample bind helloWorldEP {
    any serviceLevelVariable = "";

    @http:ResourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    params (endpoint outboundEP, http:Request req, string foo) {
        map paramsMap = req.getQueryParams();
        var bar = paramsMap.bar;

        serviceLevelVariable = "static";
        globalLevelVariable = "static";
    }
}

