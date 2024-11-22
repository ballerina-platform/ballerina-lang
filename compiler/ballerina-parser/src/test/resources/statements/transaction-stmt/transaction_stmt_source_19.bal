function foo() {
    transaction {
    } on fail SampleError error(message, error(msg), userCode = code, info = [info1, info2], posInfo = {row: posRow}) {
        io:println(posRow);
    }

    transaction {
        int a = 5;
    } on fail SampleError error(message, error(msg), userCode = code, info = [info1, info2], posInfo = {row: posRow}) {
        io:println(posRow);
    }

    transaction {
        transaction {
            transaction {
                string b;
            }
        }
    } on fail SampleError error(message, error(msg), userCode = code, info = [info1, info2], posInfo = {row: posRow}) {
        io:println(posRow);
    }

    transaction {
        transaction {
            transaction {
                string b;
            } on fail SampleError error(message, error(msg), userCode = code, info = [info1, info2], posInfo = {row: posRow}) {
                io:println(posRow);
            }
        }
    } on fail SampleError error(message, error(msg), userCode = code, info = [info1, info2], posInfo = {row: posRow}) {
        io:println(posRow);
    }
}
