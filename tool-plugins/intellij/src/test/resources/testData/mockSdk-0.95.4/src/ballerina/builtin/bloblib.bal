package ballerina.builtin;

@Description { value:"Converts blob to a string"}
@Param { value:"b: The blob value to be converted" }
@Param { value:"encoding: Encoding to used in blob conversion to string" }
@Return { value:"String representation of the given blob" }
public native function <blob b> toString (string encoding) (string);

