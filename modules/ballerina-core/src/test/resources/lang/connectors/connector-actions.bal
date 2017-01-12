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
    test:TestConnector testConnector = new test:TestConnector("MyParam1", "MyParam2", 5);
    return test:TestConnector.action1(testConnector);
}

function testAction2() {
    test:TestConnector testConnector = new test:TestConnector("MyParam1", "MyParam2", 5);
    test:TestConnector.action2(testConnector);
}

function testAction3() (boolean) {
    test:TestConnector testConnector = new test:TestConnector("MyParam1", "MyParam2", 5);
    return test:TestConnector.action3(testConnector);
}

function testAction2andAction3() (boolean) {
    test:TestConnector testConnector = new test:TestConnector("MyParam1", "MyParam2", 5);

    test:TestConnector.action2(testConnector);
    return test:TestConnector.action3(testConnector);
}

function testAction4(string inputParam) (string) {
    test:TestConnector testConnector = new test:TestConnector(inputParam, "MyParam2", 5);
    return test:TestConnector.action4(testConnector);
}

function testAction5(string functionArg1, string functionArg2, int functionArg3, string functionArg4) (string, string, int) {

    test:TestConnector testConnector = new test:TestConnector(functionArg1, functionArg2, functionArg3);

    return test:TestConnector.action5(testConnector, functionArg4);
}