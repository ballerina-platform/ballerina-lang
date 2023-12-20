public function test() {
    int i = 0;
    while i < 3 {
        i = i + 1;
        fail error SampleError("Transaction Failure", error("Database Error"), userCode = 20,
                            userReason = "deadlock condition");
    } on fail SampleError error(message, error(msg), userCode = code, userReason = reason) {
        io:println(msg);
    }
}
