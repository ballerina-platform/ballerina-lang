
connector TestConnector(string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1(TestConnector testConnector) (boolean){
        return action2Invoked;
    }
}

function testAction1() (boolean) {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    boolean value;

    value = TestConnector.foo(testConnector);
    return value;
}