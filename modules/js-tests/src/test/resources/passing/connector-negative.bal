function testUndefinedConnector() (boolean) {
    UndefinedConnector testConnector = create UndefinedConnector("MyParam1", "MyParam2", 5);
    boolean value;

    value = testConnector.foo(testConnector);
    return value;
}

connector TestConnector(string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1() (boolean){
        return action2Invoked;
    }
}

function testAction1() (boolean) {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    boolean value;

    value = testConnector.foo();
    return value;
}

connector TestConnector1(string param1) {

    boolean b1;

    action foo() (boolean){
        return b1;
    }
}

connector TestConnector1(string param1, string param2) {

    boolean b2;

    action bar() (boolean){
        return b2;
    }
}

connector TestConnector2(string param1, string param2, int param3) {

    boolean action2Invoked;

    action foo() (boolean){
        return action2Invoked;
    }

    action foo() {
        action2Invoked = true;
    }
}