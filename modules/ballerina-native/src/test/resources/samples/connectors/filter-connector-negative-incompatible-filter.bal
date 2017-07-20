import ballerina.lang.system;


connector TestConnector(string param1) {

    action action1(TestConnector testConnector, message msg) (string){
        system:println("action1 in test connector started " + param1);
          return "100";
    }

    action action2(TestConnector testConnector, message msg) (string) {
    	system:println("action2 in test connector started " + param1);
    	return "value from action2";
    }

}

connector FilterConnector<TestConnector t>(string param1) {

    action action1(FilterConnector testConnector, message msg)(int) {
          system:println("Worker in filter connector test started within filter connector " + param1);
          int value = TestConnector.action1(t, msg);
          return value;
    }

    action action2(FilterConnector testConnector, message msg)(string) {
    	  system:println("Starting action 2 within filter connector");
    	  return "TTTTTTTTTTT";
    }

}

function testArgumentPassing (string var1) (int) {
    TestConnector testConnector = create TestConnector("MyParam1") with FilterConnector("MyTest1");
    message request = {};
    int value;
    value = TestConnector.action1(testConnector, request);
    system:println("Value returned from action invocation is " + value);
    return value;

}

