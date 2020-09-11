import ballerina/io;

class MyRetryManager {
   public function shouldRetry(error e) returns boolean {
      return true;
   }
}

function testRetryWithCustomRetryManager() {
    retry<MyRetryMgr> ("test") {
        io:println("Retry task triggered!");
    }
}

function testRetryWithInvalidArgs() {
    retry<MyRetryManager> (value) {
        io:println("Retry task triggered!");
    }
}
