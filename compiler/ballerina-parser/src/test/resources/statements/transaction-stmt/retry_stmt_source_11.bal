public function test() {
    retry {

    } on fail var error(message, cause, userCode = code, userReason = reason) {
        int a = code
    }
}
