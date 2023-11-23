public function test() {
    retry {
        fail error("error!");
    } on fail error {failError: err} {
        io:println(err);
    }
}
