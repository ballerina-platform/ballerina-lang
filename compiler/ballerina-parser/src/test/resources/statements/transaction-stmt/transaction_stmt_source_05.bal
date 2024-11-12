function foo() {
    transaction {
    } on fail var error(m) {
        io:println(m);
    }

    transaction {
        int a = 5;
    } on fail var error(m) {
        io:println(m);
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail var error(m) {
        io:println(m);
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail var error(m) {
                io:println(m);
            }
        }
    } on fail var error(m) {
        io:println(m);
    }
}
