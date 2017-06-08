package ballerina.lang.readers;

import ballerina.doc;

@doc:Description { value:"Get the next record of a csv file as an array"}
@doc:Param { value:"reader: The reader instance to read record from" }
@doc:Param { value:"separator: The separator used to separate records" }
@doc:Return { value:"line: The array containing the record" }
native function readCSVRecord (reader reader, string separator) (string[]);

@doc:Description { value:"Gets the next line from reader"}
@doc:Param { value:"reader: The reader to read the next line from" }
@doc:Return { value:"line: The next line read from the reader" }
native function readLine (reader reader) (string);
