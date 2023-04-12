public function test() {
    retry {

    } on fail error error(m) {
        io:println(m);
    }
}
