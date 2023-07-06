import ballerina/httpx;

function getMethods(int i, boolean b, int y) returns string[]{
    return ["get"];
}

service serviceName on new httpx:Listener(8080) {
    @httpx:ResourceConfig {
        methods: getMethods(1,true,)
    }
    resource function newResource(httpx:Caller caller, httpx:Request request) {

    }
}