import ballerina/test;
import ballerina/io;

string debugLog;
string printError;
string printInfo;
string printTrace;
string printWarn;

// This is the mock function which will replace the real function
@test:Mock {
    packageName: "ballerina/log",
    functionName: "printDebug"
}
public function mockPrintDebug(string s) {
    debugLog = s;
}

@test:Mock {
    packageName: "ballerina/log",
    functionName: "printError"
}
public function mockPrintError(string s, error? err = ()) {
    printError = s;
}

@test:Mock {
    packageName: "ballerina/log",
    functionName: "printInfo"
}
public function mockPrintInfo(string s) {
    printInfo = s;
}

@test:Mock {
    packageName: "ballerina/log",
    functionName: "printTrace"
}
public function mockPrintTrace(string s) {
    printTrace = s;
}

@test:Mock {
    packageName: "ballerina/log",
    functionName: "printWarn"
}
public function mockPrintWarn(string s) {
    printWarn = s;
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(printWarn, "warn log");
    test:assertEquals(printInfo, "info log");
    test:assertEquals(printError, "error log with cause");
}
