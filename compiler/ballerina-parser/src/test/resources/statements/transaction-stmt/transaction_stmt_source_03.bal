function foo() {
    transaction {
    } on fail error e {
        io:println("Exception thrown...");
    }

    transaction {
        int a = 5;
    } on fail error e {
        io:println("Exception thrown...");
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail error e {
        io:println("Exception thrown...");
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail error e {
                io:println("Exception thrown...");
            }
        }
    } on fail error e {
        io:println("Exception thrown...");
    }
}
