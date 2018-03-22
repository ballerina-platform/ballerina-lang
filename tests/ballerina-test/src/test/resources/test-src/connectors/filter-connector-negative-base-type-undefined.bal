import ballerina/net.http;

connector FilterConnector(string param1) {

    action action1(http:Request request) (int) {
          int value = TestConnector2.action1(t, request);
          return value;
    }

    action action2(http:Request request) (string) {
    	  return "TTTTTTTTTTT";
    }

}

function testArgumentPassing (string var1) (int) {
    endpoint<TestConnector> testConnector {
        create TestConnector("MyParam1");
    }
    http:Request request = {};
    int value;
    value = testConnector.action1(request);
    return value;

}

