import ballerina/http;

listener http:Listener helloWorldEP = new (9090);

any globalLevelVariable = "";
service sample on helloWorldEP {
    any serviceLevelVariable = "";

    @http:ResourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    resource function params (http:Caller, http:Request req, string foo) {
        map paramsMap = req.getQueryParams();
        var bar = paramsMap.bar;

        serviceLevelVariable = foo;
        globalLevelVariable = foo;
    }
}

