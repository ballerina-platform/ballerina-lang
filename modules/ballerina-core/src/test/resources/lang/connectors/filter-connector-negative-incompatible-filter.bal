
connector TestConnector(string param1) {

    action action1(message msg) (string){
          return "100";
    }

    action action2(message msg) (string) {
    	return "value from action2";
    }

}

connector FilterConnector<TestConnector t>(string param1) {

    action action1(message msg)(int) {
          int value = t.action1(msg);
          return value;
    }

    action action2(message msg)(string) {
    	  return "TTTTTTTTTTT";
    }

}

function testArgumentPassing (string var1) (int) {
    TestConnector testConnector = create TestConnector("MyParam1") with FilterConnector("MyTest1");
    message request = {};
    int value;
    value = testConnector.action1(request);
    return value;

}

