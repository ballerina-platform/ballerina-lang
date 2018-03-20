import ballerina.net.http;

endpoint<http:Service> helloWorldEP {
port:9090
}

string globalLevelVariable = "";
@http:serviceConfig { endpoints:[helloWorldEP] }
service<http:Service> sample {
    string serviceLevelVariable = "";

    @http:resourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    resource params (http:ServerConnector conn, http:Request req, string foo) {
        map params = req.getQueryParams();
        var bar, _ = (string) params.bar;

        serviceLevelVariable = foo;
        globalLevelVariable = foo;
    }
}
