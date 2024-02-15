public function test() {
    lock {
        fail error("error!");
    } on fail error error(m) {
        io:println(m);
    }
}
