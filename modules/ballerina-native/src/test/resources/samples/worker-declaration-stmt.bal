import ballerina.lang.system;
import ballerina.lang.messages;

function testworker(message msg)(message) {
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
  //message msg = {};
  aa = 13;
  system:println(aa);
  msg -> sampleWorker;
  system:println("After worker");
  result <- sampleWorker;
  string s = messages:getStringPayload(result);
  system:println(s);
  return result;

}




