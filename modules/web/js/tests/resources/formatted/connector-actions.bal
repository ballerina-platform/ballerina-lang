connector TestConnector(string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1() (boolean){
        return action2Invoked;
    }

    action action2() {
        action2Invoked = true;
    }
    
    action action3() (boolean) {
        return action2Invoked;
    }

    action action4() (string) {
        return param1;
    }

    action action5(string actionParam) (string, string, int) {
        return actionParam, param2, param3;
    }
}

connector EmptyParamConnector() {
    action emptyParamConnAction (string s) (string) {
        return s;
    }
}


function testAction1() (boolean) {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    boolean value;

    value = testConnector.action1();
    return value;
}

function testAction2() {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    testConnector.action2();
}

function testAction3() (boolean) {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    boolean value;

    value = testConnector.action3();
    return value;
}

function testAction2andAction3() (boolean) {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    boolean value;

    testConnector.action2();

    value = testConnector.action3();
    return value;
}

function testAction4(string inputParam) (string) {
    TestConnector testConnector = create TestConnector(inputParam, "MyParam2", 5);
    string value;

    value = testConnector.action4();
    return value;
}

function testAction5(string functionArg1, string functionArg2, int functionArg3, string functionArg4) (string s1, string s2, int i) {
    TestConnector testConnector = create TestConnector(functionArg1, functionArg2, functionArg3);

    s1, s2, i = testConnector.action5(functionArg4);
    return;
}

function testEmptyParamAction(string inputParam) (string) {
    EmptyParamConnector emptyParamConector = create EmptyParamConnector();
    string s = emptyParamConector.emptyParamConnAction(inputParam);
    return s;
}

function testDotActionInvocation(string functionArg1, string functionArg2, int functionArg3, string functionArg4) (string s1, string s2, int i) {
    TestConnector testConnector = create TestConnector(functionArg1, functionArg2, functionArg3);

    s1, s2, i = testConnector.action5(functionArg4);
    return;
}