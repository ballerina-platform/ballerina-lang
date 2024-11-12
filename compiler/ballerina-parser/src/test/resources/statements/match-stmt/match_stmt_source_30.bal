public function test() {
    match bar {
        () => {
        }
        "east" => {
            fail error("error!");
        }
    } on fail error (m) {
        io:println(m);
    }
}
