import ballerina/lang.system;
import ballerina/lang.messages;

function testworker(message msg)(message) {
    message q;
    q = testworkerVM(msg);
    return q;
}

function testworkerVM(message msg)(message) {
  float aa;
  message result;
  int count = 1000;
  //message msg = {};
  aa = 13;
  system:println(aa);
  count, aa, msg -> sampleWorker;
  system:println("Worker in function test started");
  result <- sampleWorker;
  aa -> sampleWorker3;
  system:println(result);
  result <- sampleWorker3;
  return result;

  worker sampleWorker  {
  float amount;
  float sumD;
  int quantity;
  float a;
  json j;
  message m;
  message m2;
  quantity, a, m <- default;
  quantity, a, m -> sampleWorker2;
  j = {"name":"chanaka"};
  messages:setJsonPayload(m, j);
  sumD = 123d;
  amount = 222d;
  //quantity = 12;
  //a = 123d;
  sumD = sumD + ( amount * quantity );
  system:println(a);
  system:println(quantity);
  m -> default;
  m2 <- sampleWorker2;
  system:println(m2);

  worker sampleWorker2 {
  int quantity;
  float a;
  json j;
  message m;
  quantity, a, m <- sampleWorker;
  j = {"name":"IBM"};
  messages:setJsonPayload(m, j);
  system:println("XXXXXXXXXXXXXXX");
  system:println(a);
  system:println(quantity);
  m -> sampleWorker;
  }

  }

  worker sampleWorker3 {
  float index;
  json j;
  message m = {};
  index <- default;
  j = {"name":"WSO2"};
  messages:setJsonPayload(m, j);
  system:println("YYYYYYYYYYYYYY");
  system:println(index);
  m -> default;
  }

}
