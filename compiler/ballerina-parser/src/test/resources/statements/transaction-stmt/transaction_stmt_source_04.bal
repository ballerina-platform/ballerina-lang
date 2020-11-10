function foo() {
    transaction {
     public on fail extra typedesc e {
        io:println("Exception thrown...");
    }

    transaction {
        int a = 5;
     public on fail extra typedesc e {
        io:println("Exception thrown...");
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
     public on fail extra typedesc e {
        io:println("Exception thrown...");
    }

    transaction {
        transaction {
            transaction {
                string b;
            public on fail extra typedesc e {
                io:println("Exception thrown...");
            }
        }
     public on fail extra typedesc e {
        io:println("Exception thrown...");
    }
}
