import ballerina/http;
function name() {

}
service serviceName on new http:Listener(8080) {
    resource function newResource(http:Caller caller, http:Request request) {

    }
}
public function main(string... args) {

}
function blockLevelNodes() {
    if (true) {

    }
    while (true) {
        
    }
    int[] itemList = [];
    foreach var item in itemList {
        
    }
    http:Client endpointName = new();
    worker workerName {
        
    }
    return ();
}