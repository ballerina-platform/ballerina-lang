function testArgumentPassing(string var1)(int) {
    TestConnector testConnector = create TestConnector(var1) with FilterConnector(500);
    int count = 122;
    int value;
    value = TestConnector.action1(testConnector, count);
    return value;

}

connector TestConnector(string param1) {

    action action1(TestConnector testConnector, int msg) (int){
          return 500;
    }

    action action2(TestConnector testConnector, int msg) (string) {
    	return "value from action2";
    }

}


connector FilterConnector<TestConnector t>(int param1) {

    action action1(FilterConnector testConnector, int msg) (int){
          int x;
          x = TestConnector.action1(t, msg);
          string y;
          y = TestConnector.action2(t, msg);
          return x;
    }

    action action2(FilterConnector testConnector, int msg) (string) {
          return "value within filter";
    }

}