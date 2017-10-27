package ballerina.lang.files;

import ballerina.doc;
import ballerina.io;

@doc:Description { value:"Closes a given file and its stream"}
@doc:Param { value:"file: The file to be closed" }
public native function close (File file);

@doc:Description { value:"Retrieves the stream from a local file"}
@doc:Param { value:"file: The file to be opened" }
public native function open (File file, string accessMode);

@doc:Description { value:"Retrieves blob value of a file"}
@doc:Param { value:"file: The file to be read" }
@doc:Param { value:"number: Number of bytes to be read" }
@doc:Return { value:"data: The blob containing files read" }
@doc:Return { value:"numberRead: Number of bytes actually read" }
public native function read (File file, int number) (blob, int);

@doc:Description { value:"Copies a file from a given location to another"}
@doc:Param { value:"target: File/Directory that should be copied" }
@doc:Param { value:"destination: Location where the File/Directory should be pasted" }
public native function copy (File source, File destination);

@doc:Description { value:"Moves a file from a given location to another"}
@doc:Param { value:"target: File/Directory that should be moved" }
@doc:Param { value:"destination: Location where the File/Directory should be moved to" }
public native function move (File target, File destination);

@doc:Description { value:"Checks whether a file exists"}
@doc:Param { value:"file: The File struct of witch existence is to be checked" }
@doc:Return { value:"isExists: Boolean representing whether a file exists" }
public native function exists (File file) (boolean);

@doc:Description { value:"Deletes a file from a given location"}
@doc:Param { value:"target: File/Directory that should be deleted" }
public native function delete (File target);

@doc:Description { value:"Writes a file using the given blob"}
@doc:Param { value:"blob: Content to be written" }
@doc:Param { value:"file: Path of the file" }
public native function write (blob content, File file);

@doc:Description { value:"Writes a new line to a file using the given blob"}
@doc:Param { value:"blob: Content to be written" }
@doc:Param { value:"file: Path of the file" }
public native function writeln (blob content, File file);

@doc:Description {value:"Function return the ByteChannel related to the file"}
@doc:Param {value:"accessMode: Specifies whether the file should be opened for reading or writing (r/w)"}
@doc:Return{value:"ByteChannel which will allow to perform I/O operations"}
public native function <File file>  openChannel (string accessMode)(io:ByteChannel);

public struct File {
    string path;
}
