import ballerina/http;

service foo on new http:Listener(8080) {
    resource function fooRes(http:Caller caller, http:Request request) {
        
    }
}


function name() {
    json j = {};
    typedesc<service> xx = typedesc<foo>;
    xx.
}