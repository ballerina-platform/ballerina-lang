package ballerina.reflect;

@Description {value:"Check whether 2 values are deeply equal. Supports string, int, float, boolean, type, structs, maps,
 arrays, any, JSON. Any other type returns FALSE."}
@Param {value:"value1: The first value for equality."}
@Param {value:"value2: The second value for equality."}
@Return {value:"TRUE if values are deeply equal, else FALSE."}
public native function deepEquals (any value1, any value2) (boolean);