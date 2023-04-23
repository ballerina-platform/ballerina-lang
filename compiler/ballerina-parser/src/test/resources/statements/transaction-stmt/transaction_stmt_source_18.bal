function foo() {
    transaction {
    } on fail SampleError error(message, error(msg), userCode = code, info = [info1, info2]) {
        io:println(info1);
    }

    transaction {
        int a = 5;
    } on fail SampleError error(message, error(msg), userCode = code, info = [info1, info2]) {
        io:println(info1);
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail SampleError error(message, error(msg), userCode = code, info = [info1, info2]) {
        io:println(info1);
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail SampleError error(message, error(msg), userCode = code, info = [info1, info2]) {
                io:println(info1);
            }
        }
    } on fail SampleError error(message, error(msg), userCode = code, info = [info1, info2]) {
        io:println(info1);
    }
}
