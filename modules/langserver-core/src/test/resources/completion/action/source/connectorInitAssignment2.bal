import ballerina.net.http;

connector testClientConnector (int param1,int param2) {
    action testAction (int param3,int param4) {
        http:HttpClient c;
        c = create 
    }
}