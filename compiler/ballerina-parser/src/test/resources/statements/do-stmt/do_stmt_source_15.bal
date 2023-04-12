public function test() {
    do {
        fail error("error!");
    } on fail var error(message = m) {
        io:println(m);
    }
}
