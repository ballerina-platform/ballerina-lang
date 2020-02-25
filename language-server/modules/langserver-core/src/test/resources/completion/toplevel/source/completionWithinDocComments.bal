import ballerina/http;

service testService on new http:Listener(8080) {
    # Description
    #
    # + caller - caller Parameter Description
    # + request - request Parameter Description
    # 
    resource function testResource(http:Caller caller, http:Request request) {
        
    }
}
