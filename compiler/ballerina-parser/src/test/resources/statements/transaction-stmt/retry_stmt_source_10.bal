public function test() {
    retry {

    } on fail SampleError error(message, cause, userCode = code, userReason = reason) {
        int a = code
    }
}
