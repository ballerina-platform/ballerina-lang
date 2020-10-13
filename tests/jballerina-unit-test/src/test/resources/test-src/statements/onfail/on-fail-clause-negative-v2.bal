import ballerina/io;

function testQuery() returns error?{
    int i = 0;
    while (i > 2) {
        fail getError();
        i = i + 1;
    }
    io:println("While is completed.");
}

function getError() returns error {
    error err = error("Custom Error");
    return err;
}
