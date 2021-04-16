import ballerina/log;
import ballerina/runtime;

public function main() {
    error err = error("something went wrong!");

    // Shows the main log levels that are available. By default, all log messages are logged 
    // to the console at the `INFO` level. In addition to these log levels, 
    // there are 2 additional levels named `OFF` and `ALL`.
    // `OFF` turns off logging and `ALL` enables all the log levels. The log
    // level can be configured via a Ballerina configuration file or CLI
    // parameters.
    log:printDebug("debug log");
    log:printError("error log");
    log:printError("error log with cause", err);
    log:printInfo("info log");
    log:printTrace("trace log");
    log:printWarn("warn log");
    
    // Logic constructing log messages with expensive operations can alternatively be passed as a function
    // pointer implementation. The function will be executed if and only if that particular log level is enabled.
    log:printDebug(function() returns string {
        return string `Execution Context: ${runtime:getCallStack().toString()}`;
    });
}
