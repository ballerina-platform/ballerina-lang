## Module Overview

This module provides a basic API for logging.

### Loggers 

Each module in Ballerina has its own dedicated logger. A log record contains the timestamp, log level, module name, and the log message. The `printError()` function takes an optional `error` record apart from the log message. A sample log record logged from the `foo` module would look as follows:
```bash
2018-04-09 11:33:21,300 ERROR [foo] - This is an error log.
```

### Log Output

Logs are written to the `stderr` stream (i.e., the console) by default in order to make the logs more container friendly.

To publish the logs to a file, redirect the `stderr` stream to a file.
```bash
$ ballerina run program.bal 2> b7a-user.log
```

### Log Levels

This module provides functions to log at the `WARN`, `ERROR`, `INFO`, `DEBUG`, and `TRACE` levels. By default, all log messages are logged to the console at the `INFO` level. In addition to these, there are two other levels named `OFF` and `ALL`. The `OFF` log level turns off logging, and the `ALL` log level allows all log levels. The log level can be configured through the Config API.

The `b7a.log.level` configuration key can be used to configure the log level for the log API (i.e., for all the modules). The following can be provided in a configuration file.
```toml
b7a.log.level="<LOG_LEVEL>"
```

The log level can also be configured through a CLI parameter as follows:
```bash
$ ballerina run program.bal -e b7a.log.level=<LOG_LEVEL>
```

Log levels can be configured for modules either through a configuration file as `<MODULE_NAME>.loglevel="<LOG_LEVEL>"` or through a CLI parameter as `<MODULE_NAME>.loglevel=<LOG_LEVEL>`.

## Sample  

The following code snippet depicts the usage of all the functions in the log API.
```ballerina
// Logs the message at ERROR level
log:printError("error log");

// Logs the message at ERROR level, along with an error record
error e = error("error occurred");
log:printError("error log with cause", err = e);

// Logs the message at WARN level
log:printWarn("warn log");

// Logs the message at INFO level
log:printInfo("info log");

// Logs the message at DEBUG level
log:printDebug("debug log");

// Logs the message at TRACE level
log:printTrace("trace log");
```


Suppose that the above code snippet resides in a module named `foo`. We can set the log level of `foo` to `DEBUG` in a configuration file by placing the following entry in it:
```toml
[foo]
loglevel="DEBUG"

OR

foo.loglevel="DEBUG"
```

The log level of `foo` can also be configured through the CLI as follows:
```bash
$ ballerina run foo -e foo.loglevel=DEBUG
```
