import ballerina.lang.system;
import ballerina.lang.messages;

const int index = 12;

function testWorkerInVM()(message) {
    message q;
    q = testWorker();
    return q;
}

function testWorker()(message) {
  message result;
  message msg = {};
  msg -> sampleWorker;
  system:println("Worker calling function test started");
  result <- sampleWorker;
  string s = messages:getStringPayload(result);
  system:println(s);
  return result;

  worker sampleWorker {
  message result;
  message m;
  m <- default;
  result = changeMessage(m);
  result -> default;
  return;
}

}

function changeMessage(message m)(message) {
      json j;
      j = {"name":"chanaka"};
      messages:setJsonPayload(m, j);
      return m;
}