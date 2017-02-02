package samples.connectors.test;

import ballerina.lang.message;
import ballerina.lang.system;

connector TestConnector(string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1(TestConnector testConnector, message msg) (message){
          double aa;
          message result;
          worker sampleWorker (message m)  {
            double amount;
            double sumD;
            int quantity;
            double a;
            json j;

            j = `{"name":"chanaka"}`;
            message:setJsonPayload(m, j);
            sumD = 123d;
            amount = 222d;
            quantity = 12;
            a = 123d;
            sumD = sumD + ( amount * quantity );
            system:println(sumD);
            reply m;
          }
          aa = 13;
          system:println(aa);
          msg -> sampleWorker;
          system:println("After worker");
          system:println("Doing something else");
          system:println("Doing another thing");
          result <- sampleWorker;
          return result;
    }

}

function testAction1() (message) {
    test:TestConnector testConnector = new test:TestConnector("MyParam1", "MyParam2", 5);
    message request;
    message value;

    request = new message;
    value = test:TestConnector.action1(testConnector, request);
    return value;
}
