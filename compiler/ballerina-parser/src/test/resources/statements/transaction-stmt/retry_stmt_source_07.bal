function foo() {
    retry {

    } on fail {
        io:println("Exception thrown...");
    }
}
