public function test() {
    int i = 0;
    while i < 3 {
        i = i + 1;
        fail error("error!");
    } on fail var error(m {
        io:println(m);
    }
}
