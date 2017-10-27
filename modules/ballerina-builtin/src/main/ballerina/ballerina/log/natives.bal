package ballerina.log;

@Description {value: "Logs the specified value at debug level."}
@Param {value: "value: The value to be logged."}
public native function debug(any value);

@Description {value: "Logs the specified value at error level."}
@Param {value: "value: The value to be logged."}
public native function error(any value);

@Description {value: "Logs the specified value at info level."}
@Param {value: "value: The value to be logged."}
public native function info(any value);

@Description {value: "Logs the specified value at trace level."}
@Param {value: "value: The value to be logged."}
public native function trace(any value);

@Description {value: "Logs the specified value at warn level."}
@Param {value: "value: The value to be logged."}
public native function warn(any value);