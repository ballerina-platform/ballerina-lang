package ballerina.lang.regex;

import ballerina.doc;

@doc:Description { value:"Returns a string array with all sub sequences of the input string that matches the regular expression"}
@doc:Param { value:"regex: Regular expression to be applied" }
@doc:Param { value:"inputText: The string to be applied the regular expression" }
@doc:Return { value:"string[]): Matched string elements array" }
native function findAll (string regex, string inputText) (string[]);

@doc:Description { value:"Returns a Boolean value indicating whether match the entire string against the regular expression"}
@doc:Param { value:"regex: Regular expression to be applied" }
@doc:Param { value:"inputText: The string to be applied the regular expression" }
@doc:Return { value:"boolean: True if the entire string match the regular expression; false otherwise" }
native function match (string regex, string inputText) (boolean);

@doc:Description { value:"Replaces every sub sequence of the input string that matches the against the regular expression with the replacingText"}
@doc:Param { value:"inputText: The string to be applied the regular expression" }
@doc:Param { value:"regex: Regular expression to be applied" }
@doc:Param { value:"replacingText: The replacement string" }
@doc:Return { value:"string: The derived string" }
native function replaceAll (string inputText, string regex, string replacingText) (string);