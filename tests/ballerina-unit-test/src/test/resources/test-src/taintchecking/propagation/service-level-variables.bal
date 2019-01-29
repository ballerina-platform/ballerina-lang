import ballerina/http;

listener http:Listener helloWorldEP = new (9090);

any globalLevelVariable = "";
service sample on helloWorldEP {
    any serviceLevelVariable = "";

    @http:ResourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    resource function params (http:Caller caller, http:Request req, string foo) {
        map<any> paramsMap = req.getQueryParams();
        var bar = paramsMap.bar;

        self.serviceLevelVariable = "static";
        globalLevelVariable = "static";
    }
}

