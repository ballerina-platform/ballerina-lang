import ballerina/log;

function main(string... args) {
    error err = { message: "error occurred" };

    // The Ballerina log API provides functions to log at five levels, which are
    // `DEBUG`, `ERROR`, `INFO`, `TRACE`, and `WARN`. By default, all log
    // messages are logged to the console at the `INFO` level. In addition to
    // these log levels, there are 2 additional levels named `OFF` and `ALL`.
    // `OFF` turns off logging and `ALL` enables all the log levels. The log
    // level can be configured via a Ballerina configuration file or CLI
    // parameters.
    log:printDebug("debug log");
    log:printError("error log");
    log:printError("error log with cause", err = err);
    log:printInfo("info log");
    log:printTrace("trace log");
    log:printWarn("warn log");
    // To set the log level of the API, use the following CLI parameter: <br>
    // `-e b7a.log.level=[LOG_LEVEL]`
    //
    // To configure using a configuration file, place the entry given below in
    // the file:
    //
    // ```
    // [b7a.log]
    // level="[LOG_LEVEL]"
    // ```

    // Each package can also be assigned its own log level. To assign a
    // log level to a package, provide the following configuration
    // `<PACKAGE_NAME>.loglevel`.
    //
    // e.g. `-e foo.loglevel=DEBUG`
}
