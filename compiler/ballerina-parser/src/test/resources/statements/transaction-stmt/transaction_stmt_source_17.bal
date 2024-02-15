function foo() {
    transaction {
    } on fail SampleError error(message, error(msg), userCode = code, userReason = reason) {
        io:println(msg);
    }

    transaction {
        int a = 5;
    } on fail SampleError error(message, error(msg), userCode = code, userReason = reason) {
        io:println(msg);
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail SampleError error(message, error(msg), userCode = code, userReason = reason) {
        io:println(msg);
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail SampleError error(message, error(msg), userCode = code, userReason = reason) {
                io:println(msg);
            }
        }
    } on fail SampleError error(message, error(msg), userCode = code, userReason = reason) {
        io:println(msg);
    }
}
