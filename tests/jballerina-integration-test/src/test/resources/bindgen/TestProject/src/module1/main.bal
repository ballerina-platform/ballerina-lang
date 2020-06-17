import ballerina/io;

public function main() {
    io:println("Hello World!");
    error? err1 = testArrayUtils();
    error? err2 = testExceptionHandling();
    if (err1 is error) {
        io:println(err1);
    } else {
        io:println("Array util tests successful.");
    }
    if (err2 is error) {
        io:println(err2);
    } else {
        io:println("Exception handling tests successful.");
    }
}

function testExceptionHandling() returns error? {
    File file2 = newFile2("./src/test3");
    FileInputStream|FileNotFoundException fileInput = newFileInputStream1(file2);
    if (fileInput is error) {
        io:println(fileInput.message());
        io:println(fileInput);
    } else {
        return error("Error in exception handling.");
    }
}

function testArrayUtils() returns error? {
    File file = newFile2("./src/module1");
    string[] files = check file.list();
    io:println(files);

    File[] filePaths = check file.listFiles();
    foreach File x in filePaths {
        io:println(x.getPath());
    }

    String str = newString13("hello");
    byte[] strBytes = check str.getBytes();
    io:println(strBytes);

    JArithmeticException aexception = newJArithmeticException1();
    StackTraceElement trace1 = newStackTraceElement1("ab", "cd", "ef", 5);
    StackTraceElement trace2 = newStackTraceElement1("gh", "ij", "kl", 7);
    StackTraceElement[] stackTrace = [trace1, trace2];
    _ = check aexception.setStackTrace(stackTrace);
    _ = aexception.printStackTrace();
}
