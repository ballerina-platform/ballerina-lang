public function test() {
    int i = 0;
    do {
        i = i + 1;
        error err = error("Custom error thrown explicitly.");
        fail err;
    } on fail error e {
        io:println("Exception thrown...");
    }
}
