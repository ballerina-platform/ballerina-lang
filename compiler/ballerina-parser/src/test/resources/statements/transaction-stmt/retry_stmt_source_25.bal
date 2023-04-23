public function test() {
    retry {
        fail error("error!");
    } on fail int error(err) {
        io:println(err);
    }
}
