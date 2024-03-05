public function test() {
    match bar {
        () => {
        }
        "east" => {
            fail error("error!");
        }
    } on fail error [err] {
        io:println(err);
    }
}
