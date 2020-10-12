import ballerina/io;

function testVariableScope() returns error?{
        do {
            io:println("This is inside do");
            int i = 0;
            fail getError();
        } on fail error e {
            io:println("This is inner on-fail" + i.toString());
        }
    io:println("While is completed.");
}

// Unreachable code prints twice
// Fix required https://github.com/ballerina-platform/ballerina-lang/issues/26201
//function testQuery() returns error?{
//    int i = 0;
//    while (i > 2) {
//        fail getError();
//        i = i + 1;
//    }
//    io:println("While is completed.");
//}

function getError() returns error {
    error err = error("Custom Error");
    return err;
}
