package ballerina.utils.logger;

import ballerina.doc;
import ballerina.lang.errors;

@doc:Description {value: "Logs the specified value at debug level."}
@doc:Param {value: "msg: The message to be logged."}
native function debug(string msg);

@doc:Description {value: "Logs the specified message at error level."}
@doc:Param {value: "msg: The message to be logged."}
native function error(string msg);

@doc:Description {value: "Logs the specified message at error level."}
@doc:Param {value: "msg: The message to be logged."}
@doc:Param {value: "err: The error to be logged."}
native function errorCause(string msg, errors:Error err);

@doc:Description {value: "Logs the specified message at info level."}
@doc:Param {value: "msg: The message to be logged."}
native function info(string msg);

@doc:Description {value: "Logs the specified message at trace level."}
@doc:Param {value: "msg: The message to be logged."}
native function trace(string msg);

@doc:Description {value: "Logs the specified message at warn level."}
@doc:Param {value: "msg: The message to be logged."}
native function warn(string msg);