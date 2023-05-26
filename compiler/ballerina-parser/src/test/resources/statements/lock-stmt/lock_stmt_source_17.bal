public function test() {
    lock {
        fail error("error!");
    } on fail error (m) {
        io:println(m);
    }
}
