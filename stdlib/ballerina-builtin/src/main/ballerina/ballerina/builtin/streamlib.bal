package ballerina.builtin;

@Description {value:"Execute a test function"}
@Return { value:"Return value"}
public native function executeQuery (string sqlQuery);


@Description {value:"Push event to Siddhi runtime."}
@Param {value:"st: The streamlet object"}
@Param {value:"streamId: A string that specifies a streamId"}
@Param {value:"eventBody: Event body"}
public native function pushEvent (any st, string streamId, any eventBody);