public function test() {
    match bar {
        () => {
        }
        "east" => {
            fail error("error!");
        }
    } on fail int error(err) {
        io:println(err);
    }
}
