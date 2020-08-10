public function foo() {
    lock {

    } on fail error e {
        io:println("Exception thrown...");
    }
}
