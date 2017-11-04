package ballerina.log;

@Description {value: "Logs the specified value at debug level."}
@Param {value: "msg: The message to be logged."}
public native function debug(string msg);

@Description {value: "Logs the specified message at error level."}
@Param {value: "msg: The message to be logged."}
public native function error(string msg);

@Description {value: "Logs the specified message at error level."}
@Param {value: "msg: The message to be logged."}
@Param {value: "err: The error to be logged."}
public native function errorCause(string msg, error err);

@Description {value: "Logs the specified message at info level."}
@Param {value: "msg: The message to be logged."}
public native function info(string msg);

@Description {value: "Logs the specified message at trace level."}
@Param {value: "msg: The message to be logged."}
public native function trace(string msg);

@Description {value: "Logs the specified message at warn level."}
@Param {value: "msg: The message to be logged."}
public native function warn(string msg);