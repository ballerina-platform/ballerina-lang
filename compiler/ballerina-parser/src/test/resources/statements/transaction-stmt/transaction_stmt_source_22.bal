function foo() {
    transaction {
    } on fail int error(err) {
        io:println(err);
    }

    transaction {
        int a = 5;
    } on fail int error(err) {
        io:println(err);
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail int error(err) {
        io:println(err);
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail int error(err) {
                io:println(err);
            }
        }
    } on fail int error(err) {
        io:println(err);
    }
}
