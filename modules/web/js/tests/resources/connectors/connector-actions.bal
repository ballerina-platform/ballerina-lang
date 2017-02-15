package samples.connectors.test;

connector TestConnector(string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1(TestConnector testConnector) (boolean){
        return action2Invoked;
    }

    action action2(TestConnector testConnector) {
        action2Invoked = true;
    }
    
    action action3(TestConnector testConnector) (boolean) {
        return action2Invoked;
    }

    action action4(TestConnector testConnector) (string) {
        return param1;
    }

    action action5(TestConnector testConnector, string actionParam) (string, string, int) {
        return actionParam, param2, param3;
    }
}


function testAction1() (boolean) {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    boolean value;

    value = TestConnector.action1(testConnector);
    return value;
}

function testAction2() {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    TestConnector.action2(testConnector);
}

function testAction3() (boolean) {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    boolean value;

    value = TestConnector.action3(testConnector);
    return value;
}

function testAction2andAction3() (boolean) {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    boolean value;

    TestConnector.action2(testConnector);

    value = TestConnector.action3(testConnector);
    return value;
}

function testAction4(string inputParam) (string) {
    TestConnector testConnector = create TestConnector(inputParam, "MyParam2", 5);
    string value;

    value = TestConnector.action4(testConnector);
    return value;
}