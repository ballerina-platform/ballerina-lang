function testArgumentPassing(string var1)(int) {
    TestConnector testConnector = create TestConnector(var1) with FilterConnector(500);
    int count = 122;
    int value;
    value = testConnector.action1(count);
    return value;

}

connector TestConnector(string param1) {

    action action1(int msg) (int){
          return 500;
    }

    action action2(int msg) (string) {
    	return "value from action2";
    }

}


connector FilterConnector<TestConnector t>(int param1) {

    action action1(int msg) (int){
          int x;
          x = t.action1(msg);
          string y;
          y = t.action2(msg);
          return x;
    }

    action action2(int msg) (string) {
          return "value within filter";
    }

}