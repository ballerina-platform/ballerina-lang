package ballerina.lang.files;

import ballerina.io;

@Description { value:"Closes a given file and its stream"}
@Param { value:"file: The file to be closed" }
public native function close (File file);

@Description { value:"Retrieves the stream from a local file"}
@Param { value:"file: The file to be opened" }
public native function open (File file, string accessMode);

@Description { value:"Retrieves blob value of a file"}
@Param { value:"file: The file to be read" }
@Param { value:"number: Number of bytes to be read" }
@Return { value:"data: The blob containing files read" }
@Return { value:"numberRead: Number of bytes actually read" }
public native function read (File file, int number) (blob, int);

@Description { value:"Copies a file from a given location to another"}
@Param { value:"target: File/Directory that should be copied" }
@Param { value:"destination: Location where the File/Directory should be pasted" }
public native function copy (File source, File destination);

@Description { value:"Moves a file from a given location to another"}
@Param { value:"target: File/Directory that should be moved" }
@Param { value:"destination: Location where the File/Directory should be moved to" }
public native function move (File target, File destination);

@Description { value:"Checks whether a file exists"}
@Param { value:"file: The File struct of witch existence is to be checked" }
@Return { value:"isExists: Boolean representing whether a file exists" }
public native function exists (File file) (boolean);

@Description { value:"Deletes a file from a given location"}
@Param { value:"target: File/Directory that should be deleted" }
public native function delete (File target);

@Description { value:"Writes a file using the given blob"}
@Param { value:"blob: Content to be written" }
@Param { value:"file: Path of the file" }
public native function write (blob content, File file);

@Description { value:"Writes a new line to a file using the given blob"}
@Param { value:"blob: Content to be written" }
@Param { value:"file: Path of the file" }
public native function writeln (blob content, File file);

@Description {value:"Function return the ByteChannel related to the file"}
@Param {value:"accessMode: Specifies whether the file should be opened for reading or writing (r/w)"}
@Return{value:"ByteChannel which will allow to perform I/O operations"}
public native function <File file>  openChannel (string accessMode)(io:ByteChannel);

public struct File {
    string path;
}
