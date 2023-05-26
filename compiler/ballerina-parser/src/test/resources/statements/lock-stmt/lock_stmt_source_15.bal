public function test() {
    lock {
        fail error("error!");
    } on fail error error err{
io:println(err);
}
         }
