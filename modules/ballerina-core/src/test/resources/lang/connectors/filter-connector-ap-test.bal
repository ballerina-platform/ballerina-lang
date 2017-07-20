function testArgumentPassing(string var1)(int) {
    TestConnector testConnector = create TestConnector(var1) with FilterConnector(500);
    message request = {};
    int value;
    value = TestConnector.action1(testConnector, request);
    return value;

}

connector TestConnector(string param1) {

    action action1(TestConnector testConnector, message msg) (int){
          return 500;
    }

    action action2(TestConnector testConnector, message msg) (string) {
    	return "value from action2";
    }

}


connector FilterConnector<TestConnector t>(int param1) {

    action action1(FilterConnector testConnector, message msg) (int){
          int x;
          x = TestConnector.action1(t, msg);
          string y;
          y = TestConnector.action2(t, msg);
          return 500;
    }

    action action2(FilterConnector testConnector, message msg) (string) {
          return "value within filter";
    }

}