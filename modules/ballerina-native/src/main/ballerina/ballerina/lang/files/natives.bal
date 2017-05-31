package ballerina.lang.files;

import ballerina.doc;

@doc:Description { value:"Closes a given file and its stream"}
@doc:Param { value:"file: The File struct" }
native function close (File file);

@doc:Description { value:"Gets the stream from a local file"}
@doc:Param { value:"file: The file struct" }
native function open (File file);

@doc:Description { value:"Gets the reader from inputstream"}
@doc:Param { value:"file: The File struct" }
@doc:Param { value:"number: The number of bytes to be read" }
@doc:Return { value:"data: The blob containing files read" }
@doc:Return { value:"numberRead: The number of bytes actually read" }
native function readBlob (File file, int number) (blob, int);

struct File {
    string path;
}