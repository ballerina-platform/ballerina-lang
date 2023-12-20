public function test() {
    lock {
        fail error("error!");
    } on fail error {failError: err} {
        io:println(err);
    }
}
