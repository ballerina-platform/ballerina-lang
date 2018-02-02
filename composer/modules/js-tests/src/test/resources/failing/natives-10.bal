package ballerina.log;

@Description {value: "Logs the specified value at debug level."}
@Param {value: "msg: The message to be logged."}
public native function printDebug(string msg);

@Description {value: "Logs the specified message at error level."}
@Param {value: "msg: The message to be logged."}
public native function printError(string msg);

@Description {value: "Logs the specified message at error level."}
@Param {value: "msg: The message to be logged."}
@Param {value: "err: The error to be logged."}
public native function printErrorCause(string msg, error err);

@Description {value: "Logs the specified message at info level."}
@Param {value: "msg: The message to be logged."}
public native function printInfo(string msg);

@Description {value: "Logs the specified message at trace level."}
@Param {value: "msg: The message to be logged."}
public native function printTrace(string msg);

@Description {value: "Logs the specified message at warn level."}
@Param {value: "msg: The message to be logged."}
public native function printWarn(string msg);