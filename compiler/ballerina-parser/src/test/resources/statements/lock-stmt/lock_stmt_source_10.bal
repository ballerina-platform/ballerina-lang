public function foo() {
    lock {

    } on fail {
        io:println("Exception thrown...");
    }
}
