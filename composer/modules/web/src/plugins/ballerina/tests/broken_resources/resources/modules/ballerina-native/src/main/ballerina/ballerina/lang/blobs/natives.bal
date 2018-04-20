
import ballerina/doc;

@doc:Description { value:"Converts BLOB to a string"}
@doc:Param { value:"b: BLOB value to be converted" }
@doc:Param { value:"encoding: Encoding to used in blob conversion to string" }
@doc:Return { value:"string: String representation of the given BLOB" }
native function toString (blob b, string encoding) (string);

