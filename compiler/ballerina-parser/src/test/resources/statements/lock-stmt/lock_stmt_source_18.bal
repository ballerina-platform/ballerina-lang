public function test() {
    lock {
        fail error("error!");
    } on fail var (m) {
        io:println(m);
    }
}
