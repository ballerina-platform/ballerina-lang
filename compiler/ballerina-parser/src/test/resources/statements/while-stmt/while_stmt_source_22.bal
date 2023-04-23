public function test() {
    int i = 0;
    while i < 3 {
        i = i + 1;
        fail error SampleError("error!", code = 20, reason = "foo");
    } on fail error<record {int code;}> error(code = errorCode) {
        io:println(errorCode);
    }
}
