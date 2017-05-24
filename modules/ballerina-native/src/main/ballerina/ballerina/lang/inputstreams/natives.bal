package ballerina.lang.inputstreams;

import ballerina.doc;

@doc:Description { value:"Closes a given inputstream"}
@doc:Param { value:"is: The inputstream to be closed" }
native function closeInputStream (inputstream is);

@doc:Description { value:"Gets the next byte from inputstream"}
@doc:Param { value:"is: The inputstream to read next byte from" }
@doc:Return { value:"int: The int value of next byte" }
native function readByte (inputstream is) (int);

@doc:Description { value:"Gets the reader from inputstream"}
@doc:Param { value:"is: The inputstream to get reader from" }
@doc:Return { value:"reader: The reader of inputstream" }
native function getReader (inputstream is) (reader);
