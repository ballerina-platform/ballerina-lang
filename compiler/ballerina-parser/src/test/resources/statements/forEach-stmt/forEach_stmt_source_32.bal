public function test() {
    foreach var v in membersList {
        fail error("error!");
    } on fail var error(m {
        io:println(m);
    }
}
