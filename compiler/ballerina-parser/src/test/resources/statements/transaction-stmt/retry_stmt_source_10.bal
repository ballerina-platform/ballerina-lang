public function test() {
    retry {

    } on fail SampleError error(message, cause, userCode = code, userReason = reason) {
        io:println(message);
        io:println(cause);
        io:println(code);
        io:println(reason);
    }
}
