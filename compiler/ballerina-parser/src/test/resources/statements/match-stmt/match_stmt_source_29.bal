public function test() {
    match bar {
        () => {
        }
        "east" => {
            fail error("error!");
        }
    } on fail var error(message = m) {
        io:println(m);
    }
}
