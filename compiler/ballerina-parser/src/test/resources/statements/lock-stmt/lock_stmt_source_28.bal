public function test() {
    lock {
        fail error("error!");
    } on fail int error(err) {
        io:println(err);
    }
}
