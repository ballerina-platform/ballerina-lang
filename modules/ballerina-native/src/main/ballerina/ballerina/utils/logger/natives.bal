package ballerina.utils.logger;

import ballerina.doc;

@doc:Description { value:"Logs a message marked with the priority level 'debug'"}
@doc:Param { value:"msg: String value to be logged" }
native function debug (string msg);

@doc:Description { value:"Logs a message marked with the priority level 'error'"}
@doc:Param { value:"msg: String value to be logged" }
native function error (string msg);

@doc:Description { value:"Logs a message marked with the priority level 'info'"}
@doc:Param { value:"msg: String value to be logged" }
native function info (string msg);

@doc:Description { value:"Logs a message marked with the priority level 'trace'"}
@doc:Param { value:"msg: String value to be logged" }
native function trace (string msg);

@doc:Description { value:"Logs a message marked with the priority level 'warn'"}
@doc:Param { value:"msg: String value to be logged" }
native function warn (string msg);
