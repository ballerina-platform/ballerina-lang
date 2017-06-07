package ballerina.lang.files;

import ballerina.doc;

@doc:Description { value:"Closes a given file and its stream"}
@doc:Param { value:"file: The File struct" }
native function close (File file);

@doc:Description { value:"Gets the stream from a local file"}
@doc:Param { value:"file: The file struct" }
native function open (File file, string accessMode);

@doc:Description { value:"Get blob value of a file"}
@doc:Param { value:"file: The File struct" }
@doc:Param { value:"number: The number of bytes to be read" }
@doc:Return { value:"data: The blob containing files read" }
@doc:Return { value:"numberRead: The number of bytes actually read" }
native function read (File file, int number) (blob, int);

@doc:Description { value:"This function copies a file from a given location to another"}
@doc:Param { value:"target: File/Directory that should be copied" }
@doc:Param { value:"destination: The location where the File/Directory should be pasted" }
native function copy (File source, File destination);

@doc:Description { value:"This function moves a file from a given location to another"}
@doc:Param { value:"target: File/Directory that should be moved" }
@doc:Param { value:"destination: The location where the File/Directory should be moved to" }
native function move (File target, File destination);

@doc:Description { value:"This function deletes a file from a given location"}
@doc:Param { value:"target: File/Directory that should be deleted" }
native function delete (File target);

@doc:Description { value:"This function writes a file using the given blob"}
@doc:Param { value:"blob: Content" }
@doc:Param { value:"file: Path of the file" }
native function write (blob content, File file);

struct File {
    string path;
}