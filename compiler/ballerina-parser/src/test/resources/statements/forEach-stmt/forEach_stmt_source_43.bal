public function test() {
    foreach var v in membersList {
        fail error("error!");
    } on fail anydata error(err) {
        io:println(err);
    }
}
