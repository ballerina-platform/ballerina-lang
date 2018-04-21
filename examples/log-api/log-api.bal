import ballerina/log;

function main(string... args) {
    error err = {message: "error occurred"};

    // The Ballerina log API provides functions to log at five levels: `DEBUG`, `ERROR`, `INFO`, `TRACE`, and `WARN`.
    // By default, all log messages are logged to the console at the `INFO` level. In addition to these log levels,
    // there are 2 additional levels: `OFF` and `ALL`. `OFF` turns off logging and `ALL` allows all log levels.
    // The log level can be configured through a Ballerina config file or CLI parameters. <br>

    log:printDebug("debug log");
    log:printError("error log");
    log:printError("error log with cause", err=err);
    log:printInfo("info log");
    log:printTrace("trace log");
    log:printWarn("warn log");
    // To set the log level of the API, use the CLI parameter: <br>
    // `-e[ballerina.log].level=[LOG_LEVEL]` <br>

    // To configure using a config file, place the entry given below in the file: <br>
    // `[ballerina.log]` <br>
    // `level="[LOG_LEVEL]`"
}
