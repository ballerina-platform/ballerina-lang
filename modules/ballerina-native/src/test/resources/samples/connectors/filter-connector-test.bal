import ballerina.lang.messages;
import ballerina.lang.system;

function testAction1() (message) {
    TestConnector2 testConnector = create TestConnector2("MyParam1", "MyParam2", 5)
    with TestConnector("MyTest1", "MyTest2", 15);
    message request;
    message value;

    request = {};
    value = TestConnector2.action1(testConnector, request);
    return value;
}

connector TestConnector2(string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1(TestConnector2 testConnector, message msg) (message){
          float aa;
          message result;
          aa = 13;
          system:println(aa);
          msg -> sampleWorker;
          system:println("Worker in action test started " + param1);
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

    action action2(TestConnector2 testConnector, message msg) (message){
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


connector TestConnector<TestConnector2>(string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1(TestConnector testConnector, message msg) {
          float aa;
          message result;
          aa = 133;
          system:println(aa);
          msg -> sampleWorker;
          system:println("Worker in filter connector test started - XXXXXXXXXXXXXXXXXXXXXXXX " + param1);
          result <- sampleWorker;

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

    action action2(TestConnector testConnector, message msg) {
    message result;
    system:println("Starting action 2");
    result <- sampleWorker;

    worker sampleWorker {
        json j;
        j = {"name":"chanaka"};
        messages:setJsonPayload(msg, j);
        system:println(msg);
        msg -> default;
    }
    }

}

