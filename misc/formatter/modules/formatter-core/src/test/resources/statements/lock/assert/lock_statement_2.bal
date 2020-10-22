public function update() {
    int count = 0;
    foreach var i in 1 ... 1000 {
        lock {
            self.count = self.count + 1;
        } on fail var e {
            // this will be executed if the block-stmt following do fails
            // which will happen if and only if one of the two
            // check actions fails
            // type of e will be union of the error types that can be
            // returned by foo and bar
            io:println("whoops");
            return e;
        }
    }
}
