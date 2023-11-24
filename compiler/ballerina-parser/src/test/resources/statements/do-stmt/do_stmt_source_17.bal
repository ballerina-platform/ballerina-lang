public function test() {
    do {
        fail error("error!");
    } on fail var (m) {
        io:println(m);
    }
}
