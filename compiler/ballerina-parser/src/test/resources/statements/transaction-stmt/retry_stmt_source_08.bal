public function test() {
    retry {

    } on fail var error(m) {
        io:println(m);
    }
}
