public function test() {
    retry {

    } on fail error [err] {
        io:println(err);
    }
}
