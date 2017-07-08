import ballerina.lang.system;

function testArgumentPassing(string var1)(int) {
    TestConnector testConnector = create TestConnector(var1) with FilterConnector(500);
    //TestConnector testConnector = create TestConnector(var1);
    message request = {};
    int value;
    value = TestConnector.action1(testConnector, request);
    system:println("Value returned from action invocation is " + value);
    return value;

}

connector TestConnector(string param1) {

    action action1(TestConnector testConnector, message msg) (int){
        system:println("Action1 in test connector started " + param1);
          return 500;
    }

    action action2(TestConnector testConnector, message msg) (string) {
    	system:println("Starting action 2 " + param1);
    	return "value from action2";
    }

}


connector FilterConnector<TestConnector t>(int param1) {

    action action1(FilterConnector testConnector, message msg) (int){
          system:println("Action1 in filter connector started " + param1);
          int x;
          x = TestConnector.action1(t, msg);
          system:println(x);
          return 500;
    }

    action action2(FilterConnector testConnector, message msg) (string) {
    	  system:println("Starting action 2 within filter connector");
          return "value within filter";
    }

}