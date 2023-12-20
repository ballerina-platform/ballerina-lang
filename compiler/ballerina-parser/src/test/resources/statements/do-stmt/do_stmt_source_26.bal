public function test() {
    do {
        fail error("error!");
    } on fail anydata error(err) {
        io:println(err);
    }
}
