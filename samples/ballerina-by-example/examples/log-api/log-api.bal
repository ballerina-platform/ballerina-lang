import ballerina.log;

@Description {value: "The Ballerina log API provides functions to log at 5 levels: Debug, Error, Info, Trace and Warn."}
@Description {value: "By default, all log messages are logged to the console at the Info level."}
function main(string[] args) {
    log:debug("debug log");
    log:error("error log");
    log:info("info log");
    log:trace("trace log");
    log:warn("warn log");
}
