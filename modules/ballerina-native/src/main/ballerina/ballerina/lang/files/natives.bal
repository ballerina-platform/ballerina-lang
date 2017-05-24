package ballerina.lang.files;

import ballerina.doc;

@doc:Description { value:"Gets the file from path string"}
@doc:Param { value:"path: A string value representing the path of the file" }
@doc:Return { value:"file: The BFile object representing file in path" }
native function getFileFromPath (string path) (file);

@doc:Description { value:"Gets the inputstream from file"}
@doc:Param { value:"file: The BFile reference" }
@doc:Return { value:"is: The inputstream of file" }
native function getInputStream (file file) (inputstream);
