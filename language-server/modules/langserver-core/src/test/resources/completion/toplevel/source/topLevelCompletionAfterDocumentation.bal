import ballerina/http;

# Description
#
#
s
service serviceName on new http:Listener(8080) {
    resource function newResource(http:Caller caller, http:Request request) {

    }
}
