import ballerina/http;

service testService on new http:Listener(8080) {
	resource function testResource(http:Caller caller, http:Request request) {
	    http:Response response = self.testServiceFunction();	
	}
}