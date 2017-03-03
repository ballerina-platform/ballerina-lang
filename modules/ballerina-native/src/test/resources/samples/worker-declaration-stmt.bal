import ballerina.lang.system;
import ballerina.lang.messages;

function testworker(message msg)(message) {
  worker sampleWorker (message m)  {
    float amount;
    float sumD;
    int quantity;
    float a;
    json j;
    j = `{"name":"chanaka"}`;
    messages:setJsonPayload(m, j);
    sumD = 123f;
    amount = 222f;
    quantity = 12;
    a = 123f;
    sumD = sumD + ( amount * quantity );
    system:println(sumD);
    reply m;
  }
  float aa;
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




