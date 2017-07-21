import ballerina.lang.system;

function testArgumentPassing (string var1) (int) {
    TestConnector testConnector = create TestConnector(var1) with FilterConnector1(500), FilterConnector2(5.34);
    message request = {};
    int value;
    value = TestConnector.action1(testConnector, request);
    system:println("Value returned from action invocation is " + value);
    return value;

}

connector TestConnector (string param1) {

    action action1 (TestConnector testConnector, message msg) (int) {
        system:println("Action1 in test connector started " + param1);
        return 500;
    }

    action action2 (TestConnector testConnector, message msg) (string) {
        system:println("Action2 in test connector started " + param1);
        return "test connector";
    }

}


connector FilterConnector1 <TestConnector t>(int param1) {

    action action1(FilterConnector1 testConnector, message msg) (int){
        system:println("Action1 in filter connector1 started " + param1);
        int x;
        x = TestConnector.action1(t, msg);
        system:println(x);
        string y;
        y = TestConnector.action2(t, msg);
        system:println(y);
        return 500;
    }

    action action2(FilterConnector1 testConnector, message msg) (string) {
        system:println("Starting action 2 within filter connector1");
        return "filter connector1";
    }
}

connector FilterConnector2 <TestConnector t>(float param1) {
    action action1(FilterConnector2 testConnector, message msg) (int){
        system:println("Action1 in filter connector2 started " + param1);
        int x;
        x = TestConnector.action1( t, msg);
        system:println(x);
        string y;
        y = TestConnector.action2( t, msg);
        system:println(y);
        return 500;
    }

    action action2(FilterConnector2 testConnector, message msg) (string) {
        system:println("Starting action 2 within filter connector2");
        return "filter connector2";
    }
}