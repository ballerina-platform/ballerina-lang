public function test() {
    retry {
        fail error("error!");
    } on fail [error] [err] {
        io:println(err);
    }
}
