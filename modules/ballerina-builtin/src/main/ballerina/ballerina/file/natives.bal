package ballerina.file;

import ballerina.doc;
import ballerina.lang.time;

public const string R = "R";
public const string W = "W";
public const string RW = "RW";
public const string A = "A";
public const string RA = "RA";

public struct File {
    string path;
}

public struct IOError {
    string msg;
    error cause;
    StackFrame[] stackTrace;
}

public struct FileNotFoundError {
    string msg;
    error cause;
    StackFrame[] stackTrace;
}

public struct AccessDeniedError {
    string msg;
    error cause;
    StackFrame[] stackTrace;
}

public struct FileNotOpenedError {
    string msg;
    error cause;
    StackFrame[] stackTrace;
}

@doc:Description { value:"Closes a given file and its stream"}
public native function <File file> close ();

@doc:Description { value:"Retrieves the stream from a local file"}
public native function <File file> open (string accessMode);

@doc:Description { value:"Copies a file from a given location to another"}
@doc:Param { value:"target: File/Directory that should be copied" }
@doc:Param { value:"destination: Location where the File/Directory should be pasted" }
public native function copy (File source, File destination);

@doc:Description { value:"Moves a file from a given location to another"}
@doc:Param { value:"target: File/Directory that should be moved" }
@doc:Param { value:"destination: Location where the File/Directory should be moved to" }
public native function move (File target, File destination);

@doc:Description {value:"Checks whether the file exists"}
@doc:Return {value:"exists: Returns true if the file exists"}
public native function <File file> exists () (boolean);

@doc:Description { value:"Deletes a file from a given location"}
public native function <File file> delete ();

@doc:Description {value:"Checks whether the file is a directory"}
@doc:Return {value:"isDirectory: Returns true if the file is a directory"}
public native function <File file> isDirectory () (boolean);

@doc:Description {value:"Creates the directory structure specified by the file struct"}
@doc:Return {value:"Returns an AccessDeniedError if the user does not have the necessary permissions to modify the directory"}
@doc:Return {value:"Returns an IOError if the directory could not be created"}
public native function <File file> mkdirs () (boolean, AccessDeniedError, IOError);

@doc:Description {value:"Returns the last modified time of the file"}
@doc:Return {value:"Returns a Time struct"}
@doc:Return {value:"Returns an AccessDeniedError if the user does not have the necessary permissions to read the file"}
@doc:Return {value:"Returns an IOError if the file could not be read"}
public native function <File file> getModifiedTime () (time:Time, AccessDeniedError, IOError);

@doc:Description {value:"Returns the name of the file"}
@doc:Return {value:"Returns the file name as a string"}
public native function <File file> getName () (string);

@doc:Description {value:"Checks whether the user has read access to the file"}
@doc:Return {value:"Returns true if the user has read access"}
public native function <File file> isReadable () (boolean);

@doc:Description {value:"Checks whether the user has write access to the file"}
@doc:Return {value:"Returns true if the user has write access"}
public native function <File file> isWritable () (boolean);

@doc:Description {value:"Creates a new file given by the path in the File struct"}
@doc:Return {value:"Returns true if the new file was successfully created"}
@doc:Return {value:"Returns an AccessDeniedError if the user does not have the necessary permissions to create the file"}
@doc:Return {value:"Returns an IOError if the file could not be created due to an I/O error"}
public native function <File file> createNewFile () (boolean, AccessDeniedError, IOError);

@doc:Description {value:"Lists the files in the specified directory"}
@doc:Return {value:"Returns an array of File structs if successful"}
@doc:Return {value:"Returns an AccessDeniedError if the user does not have the necessary permissions to read the directory"}
@doc:Return {value:"Returns an IOError if the directory could not be opened due to an I/O error"}
public native function <File file> list () (File[], AccessDeniedError, IOError);
