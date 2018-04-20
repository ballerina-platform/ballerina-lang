
function testUndefinedConnector() (boolean) {
    UndefinedConnector testConnector = create UndefinedConnector("MyParam1", "MyParam2", 5);
    boolean value;

    value = TestConnector.foo(testConnector);
    return value;
}