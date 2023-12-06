public function test() {
    do {
        fail error("error!");
    } on fail [error] [err] {
        io:println(err);
    }
}
