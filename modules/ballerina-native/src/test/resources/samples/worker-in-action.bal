import ballerina.lang.messages;
import ballerina.lang.system;

connector TestConnector(string param1, string param2, int param3) {

    boolean action2Invoked;

    action action1(TestConnector testConnector, message msg) (message){
          worker sampleWorker (message m)  {
            double amount;
            double sumD;
            int quantity;
            double a;
            json j;

            j = `{"name":"chanaka"}`;
            messages:setJsonPayload(m, j);
            sumD = 123d;
            amount = 222d;
            quantity = 12;
            a = 123d;
            sumD = sumD + ( amount * quantity );
            system:println(sumD);
            reply m;
          }
          double aa;
          message result;

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
    TestConnector testConnector = create TestConnector("MyParam1", "MyParam2", 5);
    message request;
    message value;

    request = {};
    value = TestConnector.action1(testConnector, request);
    return value;
}
