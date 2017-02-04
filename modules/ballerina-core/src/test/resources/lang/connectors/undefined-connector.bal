package samples.connectors.test;

function testUndefinedConnector() (boolean) {
    test:UndefinedConnector testConnector = new test:UndefinedConnector("MyParam1", "MyParam2", 5);
    boolean value;

    value = test:TestConnector.foo(testConnector);
    return value;
}