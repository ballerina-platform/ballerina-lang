import ballerina/net.http;

function testArgumentPassing (string var1) (int) {
    TestConnector testConnector = create TestConnector(var1) with FilterConnector1(500), FilterConnector2(5.34);
    http:Request request = {};
    int value;
    value = testConnector.action1(request);
    return value;

}

connector TestConnector (string param1) {

    action action1 (http:Request request) (int) {
        return 500;
    }

    action action2 (http:Request request) (string) {
        return "test connector";
    }

}


connector FilterConnector1 <TestConnector t>(int param1) {

    action action1(http:Request request) (int){
        int x;
        x = t.action1(request);
        string y;
        y = t.action2(request);
        return 500;
    }

    action action2(http:Request request) (string) {
        return "filter connector1";
    }
}

connector FilterConnector2 <TestConnector t>(float param1) {
    action action1(http:Request request) (int){
        int x;
        x = t.action1(request);
        string y;
        y = t.action2(request);
        return 500;
    }

    action action2(http:Request request) (string) {
        return "filter connector2";
    }
}