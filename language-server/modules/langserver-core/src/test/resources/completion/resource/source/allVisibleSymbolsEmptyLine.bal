import ballerina/http;


final int const1 = 0;


const string const2 = "test const";

service serviceName on new http:Listener(8080) {
    resource function newResource(http:Caller caller, http:Request request) {
        int var1 = 12;
        int var2 = 123;
        
    }
}