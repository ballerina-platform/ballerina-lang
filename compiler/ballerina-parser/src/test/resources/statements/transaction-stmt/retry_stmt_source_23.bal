public function test() {
    retry {
        fail error SampleError("error!", code = 20, reason = "foo");
    } on fail error<record {int code;}> error(code = errorCode) {
        io:println(errorCode);
    }
}
