function foo() {
    retry {

    } on fail error e {
        io:println("Exception thrown...");
    }
}
