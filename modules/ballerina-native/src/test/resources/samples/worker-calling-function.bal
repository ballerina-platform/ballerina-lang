import ballerina.lang.system;
import ballerina.lang.message;
// Global constants are visible to worker
const int index = 12;

function testWorker()(message) {
  worker sampleWorker (message m)  {
    message result;
    system:println("constant value is " + index);
    result = changeMessage(m);
    reply result;
  }

  message result;
  message msg = {};
  msg -> sampleWorker;
  system:println("After worker");
  result <- sampleWorker;
  string s = message:getStringPayload(result);
  system:println(s);
  return result;

}

function changeMessage(message m)(message) {
      json j;
      j = `{"name":"chanaka"}`;
      message:setJsonPayload(m, j);
      return m;
}