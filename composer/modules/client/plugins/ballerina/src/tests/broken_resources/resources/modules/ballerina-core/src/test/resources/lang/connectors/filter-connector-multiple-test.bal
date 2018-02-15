function testArgumentPassing (string var1) (int) {
    TestConnector testConnector = create TestConnector(var1) with FilterConnector1(500), FilterConnector2(5.34);
    message request = {};
    int value;
    value = TestConnector.action1(testConnector, request);
    return value;

}

connector TestConnector (string param1) {

    action action1 (TestConnector testConnector, message msg) (int) {
        return 500;
    }

    action action2 (TestConnector testConnector, message msg) (string) {
        return "test connector";
    }

}


connector FilterConnector1 <TestConnector t>(int param1) {

    action action1(FilterConnector1 testConnector, message msg) (int){
        int x;
        x = TestConnector.action1(t, msg);
        string y;
        y = TestConnector.action2(t, msg);
        return 500;
    }

    action action2(FilterConnector1 testConnector, message msg) (string) {
        return "filter connector1";
    }
}

connector FilterConnector2 <TestConnector t>(float param1) {
    action action1(FilterConnector2 testConnector, message msg) (int){
        int x;
        x = TestConnector.action1( t, msg);
        string y;
        y = TestConnector.action2( t, msg);
        return 500;
    }

    action action2(FilterConnector2 testConnector, message msg) (string) {
        return "filter connector2";
    }
}