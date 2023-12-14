public function test() {
    match bar {
        () => {
        }
        "east" => {
            fail error("error!");
        }
    } on fail var error(m) {
        io:println(m);
    }
}
