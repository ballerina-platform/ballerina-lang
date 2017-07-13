import ballerina.lang.system;

function testCompositeConnector()(int) {
    TestLBConnector testLB = create TestLBConnector ([create TestConnector("URI1"),
                        create TestConnector("URI2")], "RoundRobin");
    message request = {};
    int value;
    value = TestLBConnector.action1(testLB, request);
    system:println("Value returned from action invocation is " + value);
    return value;

}

connector TestConnector(string param1) {

    action action1(TestConnector testConnector, message msg) (int){
        system:println("Action1 in test connector started " + param1);
        return 500;
    }

    action action2(TestConnector testConnector, message msg) (string) {
        system:println("Starting action 2 " + param1);
        return "value from action2";
    }

}

connector TestLBConnector(TestConnector[] testConnectorArray, string algorithm) {

    int count = 0;

    action action1(TestLBConnector testConnector, message msg) (int){
        int index = count % testConnectorArray.length;
        TestConnector t1 = testConnectorArray[index];
        count = count + 1;
        TestConnector.action1(t1, msg);
        system:println("Action1 in test connector started " + algorithm);
        return 500;
    }

    action action2(TestLBConnector testConnector, message msg) (string) {
        system:println("Starting action 2 " + algorithm);
        return "value from action2";
    }
}
