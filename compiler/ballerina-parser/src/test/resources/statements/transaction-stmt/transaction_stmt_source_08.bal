function foo() {
    transaction {
    } on fail var error(message, cause, userCode = code, userReason = reason) {
        io:println(message);
        io:println(cause);
        io:println(code);
        io:println(reason);
    }

    transaction {
        int a = 5;
    } on fail var error(message, cause, userCode = code, userReason = reason) {
        io:println(message);
        io:println(cause);
        io:println(code);
        io:println(reason);
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail var error(message, cause, userCode = code, userReason = reason) {
        io:println(message);
        io:println(cause);
        io:println(code);
        io:println(reason);
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail var error(message, cause, userCode = code, userReason = reason) {
                io:println(message);
                io:println(cause);
                io:println(code);
                io:println(reason);
            }
        }
    } on fail var error(message, cause, userCode = code, userReason = reason) {
        io:println(message);
        io:println(cause);
        io:println(code);
        io:println(reason);
    }
}
