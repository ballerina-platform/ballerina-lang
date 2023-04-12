public function test() {
    foreach var v in membersList {
        fail error("error!");
    } on fail var error(message = m) {
        io:println(m);
    }
}
