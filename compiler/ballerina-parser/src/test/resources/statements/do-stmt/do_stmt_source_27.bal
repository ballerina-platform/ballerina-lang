public function test() {
    do {
        fail error("error!");
    } on fail int error(err) {
        io:println(err);
    }
}
