import ballerina/http;

listener http:Listener helloWorldEP = new (19090);

any globalLevelVariable = "";
service sample on helloWorldEP {
    any serviceLevelVariable = "";
    @tainted any taintedServiceVar = "";

    resource function params (http:Caller caller, http:Request req) {
        map<any> paramsMap = req.getQueryParams();
        var bar = paramsMap.bar;
        self.taintedServiceVar = bar;

        self.serviceLevelVariable = "static";
        globalLevelVariable = "static";
    }
}

