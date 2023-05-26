public function test() {
    do {
        SampleError err = error("Transaction Failure", error("Database Error"), userCode = 20,
                            userReason = "deadlock condition");
        fail err;
    } on fail var error(message, cause, userCode = code, userReason = reason) {
        string msg = message;
    }
}
