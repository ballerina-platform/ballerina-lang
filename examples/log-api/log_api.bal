import ballerina/log;
import ballerina/io;

public function main() {
    error e = error("error occurred");

    // The Ballerina log API provides functions to log at five levels, which are
    // `DEBUG`, `ERROR`, `INFO`, `TRACE`, and `WARN`. By default, all log
    // messages are logged to the console at the `INFO` level. In addition to
    // these log levels, there are 2 additional levels named `OFF` and `ALL`.
    // `OFF` turns off logging and `ALL` enables all the log levels. The log
    // level can be configured via a Ballerina configuration file or CLI
    // parameters.
    log:printDebug("debug log");
    log:printError("error log");
    log:printError("error log with cause", e);
    log:printInfo("info log");
    log:printTrace("trace log");
    log:printWarn("warn log");
    // To set the log level of the API, use the following CLI parameter: <br>
    // `--b7a.log.level=[LOG_LEVEL]`
    //
    // To configure using a configuration file, place the entry given below in
    // the file:
    //
    // ```
    // [b7a.log]
    // level="[LOG_LEVEL]"
    // ```

    // Each module can also be assigned its own log level. To assign a
    // log level to a module, provide the following configuration
    // `<MODULE_NAME>.loglevel`.
    //
    // E.g., `--foo.loglevel=DEBUG`
    Fruit apple = new("Apple");
    Fruit orange = new("Orange");

    log:printDebug("Name of the fruit is Strawberry.");
    log:printDebug(io:sprintf("Names of the fruits are %s, %s.", apple.getName(), orange.getName()));
    // Logic constructing log messages with expensive operations can alternatively be passed as a function
    // pointer implementation. The function will be executed if and only if that particular log level is enabled.
    log:printDebug(function() returns string {
        return io:sprintf("Name of the fruit is is %s", apple.getName());
    });
}

public type Fruit object {
     string name;
     public function __init(string name) {
	    self.name = name;
     }
     function getName() returns string {
        return self.name;
     }
};
