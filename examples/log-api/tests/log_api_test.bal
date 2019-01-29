import ballerina/test;
import ballerina/io;

string printDebug = "";
string printError = "";
string printInfo = "";
string printTrace = "";
string printWarn = "";

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/log",
    functionName: "printDebug"
}
public function mockPrintDebug(string | (function() returns (string)) msg) {
    if (msg is string) {
        printDebug = msg;
    }
}

@test:Mock {
    moduleName: "ballerina/log",
    functionName: "printError"
}
public function mockPrintError(string | (function() returns (string)) msg, error? err = ()) {
    if (msg is string) {
        printError = msg;
    }
}

@test:Mock {
    moduleName: "ballerina/log",
    functionName: "printInfo"
}
public function mockPrintInfo(string | (function() returns (string)) msg) {
    if (msg is string) {
        printInfo = msg;
    }
}

@test:Mock {
    moduleName: "ballerina/log",
    functionName: "printTrace"
}
public function mockPrintTrace(string | (function() returns (string)) msg) {
    if (msg is string) {
        printTrace = msg;
    }
}

@test:Mock {
    moduleName: "ballerina/log",
    functionName: "printWarn"
}
public function mockPrintWarn(string | (function() returns (string)) msg) {
    if (msg is string) {
        printWarn = msg;
    }
}

@test:Config
function testFunc() {
    // Invoking the main function
    main();
    test:assertEquals(printDebug, "debug log");
    test:assertEquals(printError, "error log with cause");
    test:assertEquals(printInfo, "info log");
    test:assertEquals(printTrace, "trace log");
    test:assertEquals(printWarn, "warn log");
}
