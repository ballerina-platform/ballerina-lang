package ballerina.lang.exceptions;

import ballerina.doc;

@doc:Description { value:"Sets cause of the specified exception"}
@doc:Param { value:"e: The exception object" }
@doc:Param { value:"cause: The exception cause to be added" }
native function setCause (exception e, exception cause);

@doc:Description { value:"Set the category of an exception."}
@doc:Param { value:"e: The exception object" }
@doc:Param { value:"category: The exception category to be added" }
native function setCategory (exception e, string category);

@doc:Description { value:"Set the message and the category."}
@doc:Param { value:"e: The exception object" }
@doc:Param { value:"message: The exception message to be added" }
@doc:Param { value:"category: The exception category to be added" }
native function set (exception e, string m, string category);

@doc:Description { value:"Gets the exception stack trace with the cause "}
@doc:Param { value:"e: The exception object" }
@doc:Return { value:"string) : The exception stacktrace" }
native function getStackTrace (exception e) (string);

@doc:Description { value:"Gets the category of an exception"}
@doc:Param { value:"e: The exception object" }
@doc:Return { value:"string): The exception category" }
native function getCategory (exception e) (string);

@doc:Description { value:"Sets exception message specified exception"}
@doc:Param { value:"e: The exception object" }
@doc:Param { value:"message: The exception message to be added" }
native function setMessage (exception e, string m);

@doc:Description { value:"Set message to an exception."}
@doc:Param { value:"e: The exception object" }
@doc:Return { value:"string): The exception message" }
native function getMessage (exception e) (string);

