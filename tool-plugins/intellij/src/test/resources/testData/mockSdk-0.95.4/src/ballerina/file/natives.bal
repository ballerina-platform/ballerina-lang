package ballerina.file;

import ballerina/io;

@Description { value:"The Read access mode"}
public const string R = "R";
@Description { value:"The Write access mode"}
public const string W = "W";
@Description { value:"The Read Write access mode"}
public const string RW = "RW";
@Description { value:"The Append access mode"}
public const string A = "A";
@Description { value:"The Read Append access mode"}
public const string RA = "RA";

@Description { value: "Represents a file in the file system and can perform various file operations on this."}
@Field { value : "path: The path of the file"}
public struct File {
    string path;
}

@Description { value: "Represents an I/O error which could occur when processing a file."}
@Field { value : "msg: The error message"}
@Field { value : "cause: The error which caused the I/O error"}
@Field { value : "stackTrace: The stack trace of the error"}
public struct IOError {
    string msg;
    error? cause;
    StackFrame[] stackTrace;
}

@Description { value: "Represents an error which occurs when attempting to perform operations on a non-existent file."}
@Field { value : "msg: The error message"}
@Field { value : "cause: The error which caused the file not found error"}
@Field { value : "stackTrace: The stack trace of the error"}
public struct FileNotFoundError {
    string msg;
    error? cause;
    StackFrame[] stackTrace;
}

@Description { value: "Represents an error which occurs when attempting to perform operations on a file without the required privileges."}
@Field { value : "msg: The error message"}
@Field { value : "cause: The error which caused the access denied error"}
@Field { value : "stackTrace: The stack trace of the error"}
public struct AccessDeniedError {
    string msg;
    error? cause;
    StackFrame[] stackTrace;
}

@Description { value: "Represents an error which occurs when attempting to perform operations on a file without opening it."}
@Field { value : "msg: The error message"}
@Field { value : "cause: The error which caused the file not opened error"}
@Field { value : "stackTrace: The stack trace of the error"}
public struct FileNotOpenedError {
    string msg;
    error? cause;
    StackFrame[] stackTrace;
}

@Description { value:"Closes a given file and its stream"}
@Param { value: "file: The file to be closed"}
public native function <File file> close ();

@Description { value:"Retrieves the stream from a local file"}
@Param { value:"file: The file which needs to be opened" }
@Param { value:"accessMode: The file access mode used when opening the file" }
public native function <File file> open (string accessMode);

@Description { value:"Copies a file from a given location to another"}
@Param { value:"source: File/Directory that should be copied" }
@Param { value:"destination: Destination directory or path to which the source should be copied" }
public native function copy (File source, File destination);

@Description { value:"Moves a file from a given location to another"}
@Param { value:"target: File/Directory that should be moved" }
@Param { value:"destination: Location where the File/Directory should be moved to" }
public native function move (File target, File destination);

@Description {value:"Checks whether the file exists"}
@Param { value: "file: The file to be checked for existence"}
@Return { value:"Returns true if the file exists"}
public native function <File file> exists () (boolean);

@Description { value:"Deletes a file from a given location"}
@Param { value: "file: The file to be deleted"}
public native function <File file> delete ();

@Description {value:"Checks whether the file is a directory"}
@Param { value: "file: The file to be checked to determine whether it is a directory"}
@Return { value:"Returns true if the file is a directory"}
public native function <File file> isDirectory () (boolean);

@Description {value:"Creates the directory structure specified by the file struct"}
@Param { value: "file: The directory or directory structure to be created"}
@Return {value:"Returns true if the directory/directories were successfully created"}
@Return {value:"Returns an AccessDeniedError if the user does not have the necessary permissions to modify the directory"}
@Return {value:"Returns an IOError if the directory could not be created"}
public native function <File file> mkdirs () (boolean, AccessDeniedError, IOError);

@Description {value:"Returns the last modified time of the file"}
@Param { value: "file: The file of which the modified time needs to be checked"}
@Return {value:"Returns a Time struct"}
@Return {value:"Returns an AccessDeniedError if the user does not have the necessary permissions to read the file"}
@Return {value:"Returns an IOError if the file could not be read"}
public native function <File file> getModifiedTime () (Time, AccessDeniedError, IOError);

@Description {value:"Returns the name of the file"}
@Param { value: "file: The file of which the name needs to be looked up"}
@Return {value:"Returns the file name as a string"}
public native function <File file> getName () (string);

@Description {value:"Checks whether the user has read access to the file"}
@Param { value: "file: The file to be checked for read permission"}
@Return {value:"Returns true if the user has read access"}
public native function <File file> isReadable () (boolean);

@Description {value:"Checks whether the user has write access to the file"}
@Param { value: "file: The file to be checked for write permission"}
@Return {value:"Returns true if the user has write access"}
public native function <File file> isWritable () (boolean);

@Description {value:"Creates a new file given by the path in the File struct"}
@Param { value: "file: The file to be created"}
@Return {value:"Returns true if the new file was successfully created"}
@Return {value:"Returns an AccessDeniedError if the user does not have the necessary permissions to create the file"}
@Return {value:"Returns an IOError if the file could not be created due to an I/O error"}
public native function <File file> createNewFile () (boolean, AccessDeniedError, IOError);

@Description {value:"Lists the files in the specified directory"}
@Param { value: "file: The directory whose files list is needed"}
@Return {value:"Returns an array of File structs if successful"}
@Return {value:"Returns an AccessDeniedError if the user does not have the necessary permissions to read the directory"}
@Return {value:"Returns an IOError if the directory could not be opened due to an I/O error"}
public native function <File file> list () (File[], AccessDeniedError, IOError);

@Description {value:"Function to return a ByteChannel related to the file. This ByteChannel can then be used to read/write from/to the file."}
@Param { value: "file: The file to which a channel needs to be opened"}
@Param {value:"accessMode: Specifies whether the file should be opened for reading or writing (r/w)"}
@Return{value:"ByteChannel which will allow to perform I/O operations"}
public native function <File file>  openChannel (string accessMode)(io:ByteChannel);