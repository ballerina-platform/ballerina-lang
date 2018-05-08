
import ballerina/doc;

@doc:Description { value:"Prints a 'any' value to the STDOUT"}
@doc:Param { value:"a: any value to be printed" }
native function print (any a);

@doc:Description { value:"Gets the current system time in epoch format"}
@doc:Return { value:"int: System time in epoch time" }
native function epochTime () (int);

@doc:Description { value:"Logs a float value"}
@doc:Param { value:"logLevel: Log level: 1 - Trace, 2 - Debug, 3 - Info, 4 - Warn, 5 - Error" }
@doc:Param { value:"value: any value to be logged" }
native function log (int logLevel, any value);

@doc:Description { value:"Gets the value of the specified environment variable."}
@doc:Param { value:"key: The environment variable" }
@doc:Return { value:"string): The value of the specified environment variable" }
native function getEnv (string key) (string);

@doc:Description { value:"Gets the current system time in milliseconds"}
@doc:Return { value:"int: System time in milliseconds" }
native function currentTimeMillis () (int);

@doc:Description { value:"Gets the current system time in nanoseconds"}
@doc:Return { value:"int: System time in nanoseconds" }
native function nanoTime () (int);

native function getDateFormat (string format) (string);

@doc:Description { value:"Prints an any value to the STDOUT in a new line"}
@doc:Param { value:"a: any value to be printed" }
native function println (any a);

