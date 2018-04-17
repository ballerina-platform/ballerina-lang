import ballerina/utils.logger;

@doc:Description {value: "The new Ballerina log API provides functions to log at 5 levels: Debug, Error, Info, Trace and Warn."}
@doc:Description {value: "By default, all log messages are logged to the console at the Info level."}
function main(string... args) {
    logger:debug("debug log");
    logger:error("error log");
    logger:info("info log");
    logger:trace("trace log");
    logger:warn("warn log");
}
