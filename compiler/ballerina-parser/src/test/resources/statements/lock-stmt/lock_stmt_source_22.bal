public function test() {
    lock {
        fail error("error!");
    } on fail [error] [err] {
        io:println(err);
    }
}
