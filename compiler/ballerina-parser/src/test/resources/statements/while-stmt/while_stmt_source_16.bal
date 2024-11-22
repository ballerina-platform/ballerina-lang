public function test() {
    int i = 0;
    while i < 3 {
        i = i + 1;
        fail error("error!");
    } on fail error {failError: err} {
        io:println(err);
    }
}
