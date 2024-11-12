public function test() {
    retry {

    } on fail error (m) {
        io:println(m);
    }
}
