function foo() {
    transaction {
    } on fail SampleError error(message, cause, userCode = code, userReason = reason) {
        int a = code
    }

    transaction {
        int a = 5;
    } on fail SampleError error(message, cause, userCode = code, userReason = reason) {
        int a = code
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail SampleError error(message, cause, userCode = code, userReason = reason) {
        int a = code
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail SampleError error(message, cause, userCode = code, userReason = reason) {
                int a = code
            }
        }
    } on fail SampleError error(message, cause, userCode = code, userReason = reason) {
        int a = code
    }
}
