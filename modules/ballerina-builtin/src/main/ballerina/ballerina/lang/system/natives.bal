package ballerina.lang.system;

import ballerina.doc;

@doc:Description { value:"Gets the current system time in epoch format"}
@doc:Return { value:"int: System time in epoch time" }
public native function epochTime () (int);

@doc:Description { value:"Gets the current system time in milliseconds"}
@doc:Return { value:"int: System time in milliseconds" }
public native function currentTimeMillis () (int);

@doc:Description { value:"Gets the current system time in nanoseconds"}
@doc:Return { value:"int: System time in nanoseconds" }
public native function nanoTime () (int);

public native function getDateFormat (string format) (string);


