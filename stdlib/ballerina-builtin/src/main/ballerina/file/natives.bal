// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package ballerina.file;

import ballerina/time;
import ballerina/io;

@Description { value: "Represents an I/O error that could occur when processing a file"}
public type IOError {
    string message;
    error? cause;
};

@Description {value: "Reference to the file location" }
public type Path object{
    private {
      string link;
    }

    new (link, string...sLinks){
        init(link, sLinks);
    }

    @Description { value: "Constructs the path"}
    native function init(string link,string[] sLink);

    @Description { value: "Retrieves the absolute path from the provided location"}
    @Return {value:"Returns the absolute path reference or an error if the path cannot be derived"}
    public native function toAbsolutePath() returns (Path);

    @Description { value: "Retreives the absolute path from the provided location"}
    @Return {value:"Returns the absolute path string value"}
    public native function getPathValue() returns (string);

    @Description {value: "Retreives the name of the file from the provided location"}
    @Return {value:"Returns the name of the file"}
    public native function getName() returns (string);
};

@Description { value: "Check for existance of the file"}
@Param {value: "path: Refernce to the file location"}
@Return {value: "true if the file exists"}
public native function exists(@sensitive Path path) returns (boolean);

@Description { value: "Returns the list of paths in the directory"}
@Param {value: "path: Reference to the file path location"}
@Return {value: "List of file paths in the directory or an I/O error"}
public native function list(@sensitive Path path) returns (Path [] | IOError);

@Description { value: "Returns if the provided path is a directory"}
@Param {value: "path: Reference to the file path location"}
@Return {value: "true if the given file path is a directory. It is false otherwise"}
public native function isDirectory(@sensitive Path path) returns (boolean);

@Description {value: "Deletes a file/directory from the specified path"}
@Param {value: "path: Reference to the file path location"}
@Return {value:"error if the directory/file could not be deleted"}
public native function delete(@sensitive Path path) returns (boolean | IOError);

@Description {value: "Creates a directory in the specified location"}
@Param {value: "path: Reference to the file path location"}
@Return {value : "error if the directory could not be created"}
public native function createDirectory(@sensitive Path path) returns (boolean | IOError);

@Description {value: "Creates a file in the specified location"}
@Param {value: "path: Reference to the file path location"}
@Return {value : "error if the file could not be created"}
public native function createFile(@sensitive Path path) returns (boolean | IOError);

@Description {value: "Retrieves the last modified time of the path"}
@Return {value : "Last modified time or an error if the path cannot be resolved"}
public native function getModifiedTime(@sensitive Path path) returns (time:Time | IOError);

@Description {value:"This function reads a specified number of bytes from the given channel."}
function readBytes (io:ByteChannel channel, int numberOfBytes) returns (blob, int) {
    // Here is how the bytes are read from the channel.
    var result = channel.read(numberOfBytes);
    match result {
        (blob, int) content => {
            return content;
        }
        io:IOError readError => {
            throw readError;
        }
    }
}

@Description {value:"This function writes a byte content with the given offset to a channel."}
function writeBytes (io:ByteChannel channel, blob content, int startOffset = 0) returns (int) {
    // Here is how the bytes are written to the channel.
    var result = channel.write(content,startOffset);
    match result {
        int numberOfBytesWritten => {
            return numberOfBytesWritten;
        }
        io:IOError err => {
            throw err;
        }
    }
}

@Description {value:"This function copies content from the source channel to a destination channel."}
function copyBytes(io:ByteChannel src, io:ByteChannel dst) {
    // Specifies the number of bytes that should be read from a single read operation.
    int bytesChunk = 10000;
    int numberOfBytesWritten = 0;
    int readCount = 0;
    int offset = 0;
    blob readContent;
    boolean doneCoping = false;
    try {
        // Here is how to read all the content from
        // the source and copy it to the destination.
        while (!doneCoping) {
            (readContent, readCount) = readBytes(src,1000);
            if (readCount <= 0) {
                //If no content is read, the loop is ended.
                doneCoping = true;
            }
            numberOfBytesWritten = writeBytes(dst, readContent);
        }
    } catch (error err) {
        throw err;
    }
}

public function copy(@sensitive Path srcPath,@sensitive Path dstPath) returns @tainted(IOError | boolean){
    if(!exists(srcPath)){
        IOError err = {message:"the source path does not exist"};
        return err;
    }
    if(isDirectory(srcPath)){
        //If the path is a directory we need to create an equalant in destination
        if(!exists(dstPath)){
            var result = createDirectory(dstPath);
        }
        var paths =check list(srcPath);
        foreach path in paths{
            Path destinationPath = new(dstPath.getPathValue(),path.getName());
            var result = copy(path,destinationPath);
        }
    }else{
        //If the path is a file we copy the file
        io:ByteChannel srcChannel = io:openFile(srcPath.toAbsolutePath().getPathValue(),io:MODE_R);
        io:ByteChannel dstChannel = io:openFile(dstPath.toAbsolutePath().getPathValue(),io:MODE_W);
        copyBytes(srcChannel,dstChannel);
    }
    return true;
}

public function move(@sensitive Path srcPath,@sensitive Path dstPath) returns @tainted(IOError | boolean){
    var copyResult = copy(srcPath,dstPath);
    //Once the move is complete the source will be deleted
    var deleteResult = delete(srcPath);
    return true;
}
