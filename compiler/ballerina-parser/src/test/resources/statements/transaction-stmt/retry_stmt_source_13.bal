public function test() {
    retry {

    } on fail var error(message = m) {
        io:println(m);
    }
}
