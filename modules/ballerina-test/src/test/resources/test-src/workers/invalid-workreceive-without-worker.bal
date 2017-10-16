import ballerina.lang.system;

function invalidWorkSendWithoutWorker() {
  m1->worker1;
}