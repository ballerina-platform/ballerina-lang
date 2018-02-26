import ballerina.net.http;

service<http> serviceName{
    resource resourceName (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.
    }
}

struct testStruct1 {
    int test1A;
    string test1B;
}