import ballerina.net.http;
const int const1 = 0;
const string const2 = "test const";
service<http> service1 {
    resource echo1 (http:Connection conn, http:Request inRequest) {
        int var1 = 12;
        int var2 = 123;
        
    }
}