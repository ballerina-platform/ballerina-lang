function testArgumentPassing (string var1) (int) {
    TestConnector testConnector = create TestConnector(var1) with FilterConnector1(500), FilterConnector2(5.34);
    message request = {};
    int value;
    value = testConnector.action1(request);
    return value;

}

connector TestConnector (string param1) {

    action action1 (message msg) (int) {
        return 500;
    }

    action action2 (message msg) (string) {
        return "test connector";
    }

}


connector FilterConnector1 <TestConnector t>(int param1) {

    action action1(message msg) (int){
        int x;
        x = t.action1(msg);
        string y;
        y = t.action2(msg);
        return 500;
    }

    action action2(message msg) (string) {
        return "filter connector1";
    }
}

connector FilterConnector2 <TestConnector t>(float param1) {
    action action1(message msg) (int){
        int x;
        x = t.action1(msg);
        string y;
        y = t.action2(msg);
        return 500;
    }

    action action2(message msg) (string) {
        return "filter connector2";
    }
}