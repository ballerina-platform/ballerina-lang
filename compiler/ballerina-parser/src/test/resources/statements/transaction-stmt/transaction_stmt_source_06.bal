function foo() {
    transaction {
    } on fail error error(m) {
        io:println(m);
    }

    transaction {
        int a = 5;
    } on fail error error(m) {
        io:println(m);
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail error error(m) {
        io:println(m);
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail error error(m) {
                io:println(m);
            }
        }
    } on fail error error(m) {
        io:println(m);
    }
}
