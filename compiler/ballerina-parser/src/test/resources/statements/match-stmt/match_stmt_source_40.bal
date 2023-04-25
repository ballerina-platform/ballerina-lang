public function test() {
    match bar {
        () => {
        }
        "east" => {
            fail error("error!");
        }
    } on fail anydata error(err) {
        io:println(err);
    }
}
