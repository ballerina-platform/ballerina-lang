import ballerina/http;

service<http> service1 {
    resource echo1 (http:Connection conn, http:Request inRequest) {
        
    }
}
function function1(int a, string b) {
    int testVal = a;
}
function function2() returns (int){
    int testA = 1;
    int testB = 2;
    return testA;
}
function function4() {
    string testStr = "This is Test String";
}