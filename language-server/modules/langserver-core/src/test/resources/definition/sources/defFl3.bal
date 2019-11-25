import ballerina/http;

listener http:Listener httpLst = new(9090);
listener http:Listener httpLst2 = new(9090);
http:Client cl2 = new("http://localhost:9090");
service defTestService1 on new http:Listener(8080) {
    resource function defTestResource1(http:Caller caller, http:Request request) {
        http:Client cl1 = new("http://localhost:9090");
        var payload = request.getJsonPayload();
        http:Request req1 = new();
        http:Request req2 = new();
        var res1 = cl2->get("path");
        var res2 = cl1->get("path");
        checkpanic caller->respond("test response");
    }
}

service defTestService2 on httpLst, httpLst2 {
    resource function defTestResource2(http:Caller caller, http:Request request) {
        self.service2Function();
    }
    
    function service2Function() {
		// Logic goes here
	}
}