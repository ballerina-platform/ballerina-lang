function foo() {
    transaction {
    } on fail error<record {int code;}> error(code = errorCode) {
        io:println(errorCode);
    }

    transaction {
        int a = 5;
    } on fail error<record {int code;}> error(code = errorCode) {
        io:println(errorCode);
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail error<record {int code;}> error(code = errorCode) {
        io:println(errorCode);
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail error<record {int code;}> error(code = errorCode) {
                io:println(errorCode);
            }
        }
    } on fail error<record {int code;}> error(code = errorCode) {
        io:println(errorCode);
    }
}
