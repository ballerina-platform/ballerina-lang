function foo() {
    transaction {
    } on fail anydata error(err) {
        io:println(err);
    }

    transaction {
        int a = 5;
    } on fail anydata error(err) {
        io:println(err);
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail anydata error(err) {
        io:println(err);
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail anydata error(err) {
                io:println(err);
            }
        }
    } on fail anydata error(err) {
        io:println(err);
    }
}
