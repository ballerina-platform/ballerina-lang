import ballerina/net.http;

string globalLevelVariable = "";
service<http> sample {
    string serviceLevelVariable = "";

    @http:resourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    resource params (http:Connection conn, http:Request req, string foo) {
        map params = req.getQueryParams();
        var bar, _ = (string) params.bar;

        serviceLevelVariable = "static";
        globalLevelVariable = "static";
    }
}
