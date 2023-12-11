public function test() {
    lock {
        fail error("error!");
    } on fail var error(m) {
        io:println(m);
    }
}
