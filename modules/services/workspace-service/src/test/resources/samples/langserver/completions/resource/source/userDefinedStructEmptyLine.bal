import ballerina.net.http;
service<http> service1 {
    resource echo1 (http:Request req,http:Response res) {
        
    }
}

struct testStruct1 {
    int test1A = 12;
}

struct testStruct2 {
    int test2A = 12;
}