package ballerina.lang.outputstreams;

import ballerina.doc;

@doc:Description { value:"Closes a given outputstream"}
@doc:Param { value:"is: The outputstream to be closed" }
native function closeOutputStream (outputstream os);

@doc:Description { value:"This function writes an array to a given location as a CSV file"}
@doc:Param { value:"array: String" }
@doc:Param { value:"os: output stream" }
@doc:Param { value:"encoding: Charset to used in writing CSV" }
@doc:Param { value:"applyQuotes: Apply quotes to all data" }
native function writeCSVRecord (string[] arr, outputstream os, string charset, boolean applyQuotes);

@doc:Description { value:"This function writes an input stream to an output stream"}
@doc:Param { value:"is: input stream" }
@doc:Param { value:"os: output stream" }
native function write (inputstream is, outputstream os);

@doc:Description { value:"This function writes an array to a given location as a CSV file"}
@doc:Param { value:"content: content to write" }
@doc:Param { value:"os: output stream" }
@doc:Param { value:"charset: charset to encode the content with" }
native function writeString (string content, outputstream os, string charset);