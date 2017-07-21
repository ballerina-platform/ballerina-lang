connector FilterConnector<TestConnector2 t>(string param1) {

    action action1(FilterConnector testConnector, message msg)(int) {
          int value = TestConnector2.action1(t, msg);
          return value;
    }

    action action2(FilterConnector testConnector, message msg)(string) {
    	  return "TTTTTTTTTTT";
    }

}

function testArgumentPassing (string var1) (int) {
    TestConnector testConnector = create TestConnector("MyParam1") with FilterConnector("MyTest1");
    message request = {};
    int value;
    value = TestConnector.action1(testConnector, request);
    return value;

}

