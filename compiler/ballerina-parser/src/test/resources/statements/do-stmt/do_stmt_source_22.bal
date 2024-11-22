public function test() {
    do {
        fail error SampleError("Transaction Failure", error("Database Error"), userCode = 20,
                            userReason = "deadlock condition");
    } on fail SampleError error(message, error(msg), userCode = code, userReason = reason) {
        io:println(msg);
    }
}
