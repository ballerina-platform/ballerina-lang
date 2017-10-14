package ballerina.lang.system;

import ballerina.doc;

@doc:Description { value:"Prints a 'any' value to the STDOUT"}
@doc:Param { value:"a: any value to be printed" }
public native function print (any a);

@doc:Description { value:"Gets the current system time in epoch format"}
@doc:Return { value:"int: System time in epoch time" }
public native function epochTime () (int);

@doc:Description { value:"Logs a float value"}
@doc:Param { value:"logLevel: Log level: 1 - Trace, 2 - Debug, 3 - Info, 4 - Warn, 5 - Error" }
@doc:Param { value:"value: any value to be logged" }
public native function log (int logLevel, any value);

@doc:Description { value:"Gets the value of the specified environment variable."}
@doc:Param { value:"key: The environment variable" }
@doc:Return { value:"string): The value of the specified environment variable" }
public native function getEnv (string key) (string);

@doc:Description { value:"Gets the current system time in milliseconds"}
@doc:Return { value:"int: System time in milliseconds" }
public native function currentTimeMillis () (int);

@doc:Description { value:"Gets the current system time in nanoseconds"}
@doc:Return { value:"int: System time in nanoseconds" }
public native function nanoTime () (int);

public native function getDateFormat (string format) (string);

@doc:Description { value:"Prints an any value to the STDOUT in a new line"}
@doc:Param { value:"a: any value to be printed" }
public native function println (any a);

