# Package overview

This package provides local file system operations, such as extending the `ballerina/io` package and performing I/O operations on files, and creating, deleting, modifying, and validating the files.

## Perform file I/O operations
File I/O operations, such as writing, updating, copying, and moving, can be achieved in conjunction with the `ballerina/io` package when using `Path`. 

## Create, delete, and modify files
The `ballerina/file` package  provides functions to manage metadata in the files and perform operations, such as for creating, and deleting files. The package can make the file system operations listen to a directory, identify events, and resiprocate. 

## Validate files
Validation is required to ensure that all the pre-conditions and post-conditions of an operation are satisfied. This package supports pre-validation and post-validation actions through functions such as `exists()` and `isDirectory()` respectively.

## Types of file paths
The `Path` is a unique identifier that can either be absolute or relative. The absolute path or the full path name is the location of a file relative to the root directory. The relative path indicates the location of a file relative to the current location of the execution. 
 
```ballerina
string relativePathValue = “./doc”;
file:Path relativePath = new(relativePathValue);

string absolutePathValue = “/home/user/ballerina/doc”;
file:Path absolutePath = new(absolutePathValue);
```

## Absolute paths on different operating systems
The absolute path differs from OS (Operating System ) to OS. For example, a Unix based OS defines the path as `/home/user/ballerina/examples` and a Windows based OS defines the path as `C:\windows\user\ballerina\examples`.

## Convert relative paths to absolute paths
The `toAbsolutePath()` function converts a relative path to an absolute path.

```ballerina
file:Path absolutePath = relativePath.toAbsolutePath();
```

## Access denied error
File and directories are protected entities. The file operations are restricted using various methods depending on the OS. Functions that access a protected or non-existant entity returns an `AccessDenied` error. 

# Samples

The sample given below writes new content to a file. 

The file write operation can be completed using the `openFile()` and `write()`functions that are exposed by the `ballerina/io` package. The `openFile()` function creates a streaming channel to a local file. Channels provide read or write access to different resources. The permission to perform operations on the file are defined using accessMode.

```ballerina
function testWriteFile(string pathValue,string accessMode,blob content){
file:Path filePath = new(pathValue);
//Get the absolute file path.
string absolutePath = filePath.toAbsolutePath().getPathValue();
// Open the file that is denoted by the absolute path. 
io:ByteChannel channel =io:openFile(absolutePath,accessMode);
//Write the content to the file.
var result = channel.write(content,0);
var closeResult = channel.close()
}
```

The sample give below shows how an endpoint is used to listen to the local folder and identify events, such as creating a new file.

The `onCreate()` resource method gets invoked when a file is created inside the `target/fs` folder. In addition to the above operations, `onDelete()` and `onModify()` methods can be used to listen to the delete and modify events respectively.

```ballerina
endpoint file:Listener localFolder {
    path:"target/fs"
};
service fileSystem bind localFolder {
    onCreate (file:FileEvent m) {
    }
}
```

The sample given below validates if the given path exists using the `exsits()` method. If it does exist, it checks whether it is a file or directory using the `isDirectory()` method.

```ballerina
file:Path directoryPath = new(directoryPathValue);
file:Path filePath = new(filePathValue);
// Checks if the file path exists.
var isExists = file:exists(directoryPath);
//If the path does not exist, creates a new directory in the given path.
if (!isExists) {
 var result = file: createDirectory(directoryPath);
}
// Checks if the path represents a directory or file. 
If(file: isDirectory(directoryPath)) {
 var createFileResult = file: createFile(filePath);
}
```
