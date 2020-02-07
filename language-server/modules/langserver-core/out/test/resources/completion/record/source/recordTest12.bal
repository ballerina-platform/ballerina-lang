import ballerina/http;

service testService on new http:Listener(8080) {
    @http:ResourceConfig {

        auth: {  } 
    }
    resource function testResource(http:Caller caller, http:Request request) {
        
    }
}