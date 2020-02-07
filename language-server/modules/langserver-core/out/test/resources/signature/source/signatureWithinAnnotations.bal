import ballerina/http;

function getMethods(int i, boolean b, int y) returns string[]{
    return ["get"];
}

service serviceName on new http:Listener(8080) {
    @http:ResourceConfig {
        methods: getMethods(1,true,)
    }
    resource function newResource(http:Caller caller, http:Request request) {

    }
}