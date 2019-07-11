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
        var bar = req.getQueryParamValue("bar");

        self.serviceLevelVariable = foo;
        globalLevelVariable = foo;
    }
}

