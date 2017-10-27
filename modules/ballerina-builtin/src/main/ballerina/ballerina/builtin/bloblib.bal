package ballerina.builtin;

@Description { value:"Converts BLOB to a string"}
@Param { value:"b: BLOB value to be converted" }
@Param { value:"encoding: Encoding to used in blob conversion to string" }
@Return { value:"string: String representation of the given BLOB" }
public native function <blob b> toString (string encoding) (string);

