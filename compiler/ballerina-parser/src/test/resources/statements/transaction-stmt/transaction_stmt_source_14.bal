function foo() {
    transaction {
    } on fail error {failError: err} {
        io:println(err);
    }

    transaction {
        int a = 5;
    } on fail error {failError: err} {
        io:println(err);
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail error {failError: err} {
        io:println(err);
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail error {failError: err} {
                io:println(err);
            }
        }
    } on fail error {failError: err} {
        io:println(err);
    }
}
