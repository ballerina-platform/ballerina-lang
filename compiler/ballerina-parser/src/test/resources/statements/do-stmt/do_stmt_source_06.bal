public function test() {
    int i = 0;
    do {
        i = i + 1;
        error err = error("Custom error thrown explicitly.");
        fail err;
     public on fail extra typedesc e {
        io:println("Exception thrown...");
    }
}
