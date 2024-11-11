public function test() {
    match bar {
        () => {
        }
        "east" => {
            fail error("error!");
        }
    } on fail error {failError: err} {
        io:println(err);
    }
}
