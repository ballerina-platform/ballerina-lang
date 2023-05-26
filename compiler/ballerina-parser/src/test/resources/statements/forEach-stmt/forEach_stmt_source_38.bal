public function test() {
    foreach var v in membersList {
        fail error("error!");
    } on fail [error] [err] {
        io:println(err);
    }
}
