import ballerina/lang.messages;
import ballerina/lang.system;

connector TestConnector(string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1(TestConnector testConnector, message msg) (message){
          float aa;
          message result;
          aa = 13;
          system:println(aa);
          msg -> sampleWorker;
          system:println("Worker in action test started");
          system:println("Doing something else");
          system:println("Doing another thing");
          result <- sampleWorker;
          return result;

          worker sampleWorker {
          float amount;
          float sumD;
          int quantity;
          float a;
          json j;
          message m;
          m <- default;
          j = {"name":"chanaka"};
          messages:setJsonPayload(m, j);
          sumD = 123d;
          amount = 222d;
          quantity = 12;
          a = 123d;
          sumD = sumD + ( amount * quantity );
          system:println(sumD);
          m -> default;
    }
    }

    action action2(TestConnector testConnector, message msg) (message){
    message result;
    system:println("Starting action 2");
    result <- sampleWorker;
    return result;

    worker sampleWorker {
        json j;
        j = {"name":"chanaka"};
        messages:setJsonPayload(msg, j);
        system:println(msg);
        msg -> default;
    }
    }

}

function testAction1() (message) {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    message request;
    message value;

    request = {};
    value = TestConnector.action1(testConnector, request);
    return value;
}

function testAction2() (message) {
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    message request;
    message value;

    request = {};
    value = TestConnector.action2(testConnector, request);
    return value;
}
