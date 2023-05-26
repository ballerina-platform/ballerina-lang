public function test() {
    do {
        fail error("error!");
    } on fail error {failError: err} {
        io:println(err);
    }
}
