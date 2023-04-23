function foo() {
    transaction {
    } on fail var error(message, cause, userCode = code, userReason = reason) {
        int a = code
    }

    transaction {
        int a = 5;
    } on fail var error(message, cause, userCode = code, userReason = reason) {
        int a = code
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail var error(message, cause, userCode = code, userReason = reason) {
        int a = code
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail var error(message, cause, userCode = code, userReason = reason) {
                int a = code
            }
        }
    } on fail var error(message, cause, userCode = code, userReason = reason) {
        int a = code
    }
}
