public function test() {
    lock {
        fail error("error!");
    } on fail = {
    }
}
