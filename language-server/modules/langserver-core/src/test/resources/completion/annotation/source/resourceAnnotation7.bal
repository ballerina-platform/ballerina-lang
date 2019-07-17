import ballerina/http as httpAlias;

service serviceName on new http:Listener(8080) {
    @httpAlias:
    resource function newResource(http:Caller caller, http:Request request) {
        
    }
}