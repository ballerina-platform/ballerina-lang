public function test() {
    do {
        fail error("error!");
    } on fail = {
    }
}
