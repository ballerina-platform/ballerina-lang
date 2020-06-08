// No diagnostics in the following line
import ballerina/io;
// Semicolon is missing
import ballerina/log

public function funcWithDiagnostics() {
    int a = 5;
    // semicolon is missing
    int b = 10
    return 10;
}

public function funcWithoutDiagnostics() {
    int a = 5;
    int b = 10;
    return 10;
}
