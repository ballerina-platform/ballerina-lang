public function foo() {
    foreach var v in fruits {
        int k = 2;
        k += 5;
    } on fail {
        io:println("Exception thrown...");
    }
}
