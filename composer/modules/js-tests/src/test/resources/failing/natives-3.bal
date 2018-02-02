package ballerina.net.ftp;

import ballerina.file;

@Field {value:"name: Absolute file URI for triggerd event"}
@Field {value:"size: Size of the file"}
@Field {value:"lastModifiedTimeStamp: Last modified timestamp of the file"}
public struct FTPServerEvent {
    string name;
    int size;
    int lastModifiedTimeStamp;
}

@Description {value:"FTP client connector for outbound FTP file requests"}
public connector FTPClient () {

    @Description { value:"Retrieves blob value of a file"}
    @Param { value:"file: The file to be read" }
    @Return { value:"data: The blob containing file read" }
    native action read (file:File file) (blob);

    @Description { value:"Copies a file from a given location to another"}
    @Param { value:"target: File/Directory that should be copied" }
    @Param { value:"destination: Location where the File/Directory should be pasted" }
    native action copy (file:File source, file:File destination);

    @Description { value:"Moves a file from a given location to another"}
    @Param { value:"target: File/Directory that should be moved" }
    @Param { value:"destination: Location where the File/Directory should be moved to" }
    native action move (file:File target, file:File destination);

    @Description { value:"Deletes a file from a given location"}
    @Param { value:"target: File/Directory that should be deleted" }
    native action delete (file:File target);

    @Description { value:"Writes a file using the given blob"}
    @Param { value:"blob: Content to be written" }
    @Param { value:"file: Path of the file" }
    native action write (blob content, file:File file);

    @Description { value:"Create a file or folder"}
    @Param { value:"file: Path of the file" }
    @Param { value:"isDir: Specify whether it is a file or a folder" }
    native action createFile (file:File file, boolean isDir);

    @Description { value:"Checks the existence of a file"}
    @Param { value:"file: File struct containing path information" }
    @Return { value:"boolean: The availability of the file" }
    native action exists (file:File file) (boolean);

}