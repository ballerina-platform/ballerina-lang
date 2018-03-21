import ballerina.net.http;
service<http> service1 {
    resource echo1 (http:Connection conn, http:Request inRequest) {
        
    }
}
function function1(int a, string b) {
    int testVal = a;
}
function function2() (int a,int b){
    int testA = 1;
    int testB = 2;
    return testA, testB;
}
function function3(int param1, int param2) (int a,int b){
    int testA = param1;
    int testB = param2;
    return testA, testB;
}
function function4() {
    string testStr = "This is Test String";
}