import ballerina/lang.system;

function testWorkerInVM()(int) {
    int q;
    q = testWorker();
    return q;
}

function testWorker()(int) {
  int result;
  message msg = {};
  msg -> sampleWorker;
  system:println("Worker calling function test started");
  result <- sampleWorker;
  system:println(result);
  return result;

  worker sampleWorker {
  int r = 120;
  message m;
  m <- default;
  system:println("before calling function");
  //r = changeMessage(m);
  system:println("after calling function");
  r -> default;
  return;
}

}

function changeMessage(message m)(int) {
      system:println("Within function");
      return 120;
}