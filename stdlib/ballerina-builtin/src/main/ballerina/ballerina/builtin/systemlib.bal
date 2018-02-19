package ballerina.builtin;

@Description { value:"Halt the current thread for the specified time period"}
@Param { value:"int: Sleep time in milliseconds" }
public native function sleep (int t);
