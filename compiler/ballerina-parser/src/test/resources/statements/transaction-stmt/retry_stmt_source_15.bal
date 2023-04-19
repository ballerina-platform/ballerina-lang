public function test() {
    retry {

    } on fail var (m) {
        io:println(m);
    }
}
