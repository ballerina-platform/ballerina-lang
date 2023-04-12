public function test() {
    lock {
        fail error("error!");
    } on fail var error(message = m) {
        io:println(m);
    }
}
