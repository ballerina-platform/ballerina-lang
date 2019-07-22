import ballerina/http;

listener http:Listener helloWorldEP = new (19294);

any globalLevelVariable = "";
service sample on helloWorldEP {
    any serviceLevelVariable = "";
    @tainted any taintedServiceVar = "";

    @http:ResourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    resource function params (http:Caller caller, http:Request req, string foo) {
        var bar = req.getQueryParamValue("bar");

        self.serviceLevelVariable = foo;
        globalLevelVariable = foo;
        sen(self.taintedServiceVar);
    }
}

function sen(@untainted any secureIn) {

}

