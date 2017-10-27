package ballerina.lang.strings;

import ballerina.doc;

@doc:Description { value:"Returns a string representation of an integer argument"}
@doc:Param { value:"value: An integer argument" }
@doc:Return { value:"string: String representation of the specified integer argument" }
public native function valueOf (any value) (string);