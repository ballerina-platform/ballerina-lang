public function test() {
    do {
        fail error("error!");
    } on fail error error(m) {
        io:println(m);
    }
}
