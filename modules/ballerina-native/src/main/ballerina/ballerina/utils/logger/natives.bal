package ballerina.utils.logger;

import ballerina.doc;

@doc:Description {value: "Logs the specified message at the debug level."}
@doc:Param {value: "msg: The message to be logged."}
native function debug(string msg);

@doc:Description {value: "Logs the specified message at the error level."}
@doc:Param {value: "msg: The message to be logged."}
native function error(string msg);

@doc:Description {value: "Logs the specified message at the info level."}
@doc:Param {value: "msg: The message to be logged."}
native function info(string msg);

@doc:Description {value: "Logs the specified message at the trace level."}
@doc:Param {value: "msg: The message to be logged."}
native function trace(string msg);

@doc:Description {value: "Logs the specified message at the warn level."}
@doc:Param {value: "msg: The message to be logged."}
native function warn(string msg);