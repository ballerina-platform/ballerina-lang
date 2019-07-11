import ballerina/http;

listener http:Listener helloWorldEP = new (19090);

any globalLevelVariable = "";
service sample on helloWorldEP {
    any serviceLevelVariable = "";
    @tainted any taintedServiceVar = "";

    resource function params (http:Caller caller, http:Request req) {
        var bar = req.getQueryParamValue("bar");
        self.taintedServiceVar = bar;

        self.serviceLevelVariable = "static";
        globalLevelVariable = "static";
    }
}

