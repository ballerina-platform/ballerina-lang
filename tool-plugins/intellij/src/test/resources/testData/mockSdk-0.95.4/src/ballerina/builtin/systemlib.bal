package ballerina.builtin;

@Description { value:"Prints a 'any' value to the STDOUT"}
@Param { value:"a: any value to be printed" }
public native function print (any a);

@Description { value:"Prints an any value to the STDOUT in a new line"}
@Param { value:"a: any value to be printed" }
public native function println (any a);

@Description { value:"Halt the current thread for the specified time period"}
@Param { value:"int: Sleep time in milliseconds" }
public native function sleep (int t);
