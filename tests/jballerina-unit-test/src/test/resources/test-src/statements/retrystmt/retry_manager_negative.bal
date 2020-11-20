class MyRetryManager {
   public function shouldRetry(error e) returns boolean {
      return true;
   }
}

function testRetryWithCustomRetryManager() {
    retry<MyRetryMgr> ("test") {
        // do nothing
    }
}

function testRetryWithInvalidArgs() {
    retry<MyRetryManager> (value) {
        // do nothing
    }
}
