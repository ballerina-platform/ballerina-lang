package ballerina.net.ftp;

import ballerina.lang.files;
import ballerina.doc;

connector ClientConnector () {

    @doc:Description { value:"Retrieves blob value of a file"}
    @doc:Param { value:"file: The file to be read" }
    @doc:Return { value:"data: The blob containing files read" }
    @doc:Return { value:"numberRead: Number of bytes actually read" }
    native action read (files:File file) (blob);

    @doc:Description { value:"Copies a file from a given location to another"}
    @doc:Param { value:"target: File/Directory that should be copied" }
    @doc:Param { value:"destination: Location where the File/Directory should be pasted" }
    native action copy (files:File source, files:File destination);

    @doc:Description { value:"Moves a file from a given location to another"}
    @doc:Param { value:"target: File/Directory that should be moved" }
    @doc:Param { value:"destination: Location where the File/Directory should be moved to" }
    native action move (files:File target, files:File destination);

    @doc:Description { value:"Deletes a file from a given location"}
    @doc:Param { value:"target: File/Directory that should be deleted" }
    native action delete (files:File target);

    @doc:Description { value:"Writes a file using the given blob"}
    @doc:Param { value:"blob: Content to be written" }
    @doc:Param { value:"file: Path of the file" }
    native action write (blob content, files:File file);

    @doc:Description { value:"Create a file or folder"}
    @doc:Param { value:"file: Path of the file" }
    @doc:Param { value:"isDir: Specify whether it is a file or a folder" }
    native action createFile (files:File file, boolean isDir);

    @doc:Description { value:"Checks the existence of a file"}
    @doc:Param { value:"file: File struct containing path information" }
    @doc:Return { value:"boolean: The availability of the file" }
    native action exists (files:File file) (boolean);

}