public function test() {
    retry {
        fail error("error!");
    } on fail = {
    }
}
