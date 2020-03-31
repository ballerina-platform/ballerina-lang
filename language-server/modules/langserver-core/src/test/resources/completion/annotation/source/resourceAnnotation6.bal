import ballerina/http as httpAlias;

service serviceName on new httpAlias:Listener(8080) {
    @
    resource function newResource(httpAlias:Caller caller, httpAlias:Request request) {
        
    }
}