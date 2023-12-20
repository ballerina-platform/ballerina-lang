public function test() {
    retry {
        fail error("error!");
    } on fail anydata error(err) {
        io:println(err);
    }
}
