import ballerina.lang.system;

function testArgumentPassing(string var1)(int) {
    TestConnector testConnector = create TestConnector(var1) with FilterConnector("Chanaka");
    message request = {};
    int value;
    value = TestConnector.action1(testConnector, request);
    system:println("Value returned from action invocation is " + value);
    return value;

}

connector TestConnector(string param1) {

    action action1(TestConnector testConnector, message msg) (int){
        system:println("Worker in action test started " + param1);
          return 100;
    }

    action action2(TestConnector testConnector, message msg) (string) {
    	system:println("Starting action 2 " + param1);
    	return "value from action2";
    }

}


connector FilterConnector<TestConnector>(string param1) {

    action action1(FilterConnector testConnector, message msg) {
          system:println("Worker in filter connector test started within filter connector" + param1);
    }

    action action2(FilterConnector testConnector, message msg) {
    	  system:println("Starting action 2 within filter connector");
    }

}