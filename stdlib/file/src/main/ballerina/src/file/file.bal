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

import ballerinax/java;

# Returns the current working directory.
#
# + return - Current working directory or an empty string if the current working directory cannot be determined
public function getCurrentDirectory() returns string {
    return <string>java:toString(externGetCurrentDirectory());
}

function externGetCurrentDirectory() returns handle = @java:Method {
    class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "getCurrentDirectory"
} external;

# Reports whether file or directory exists for the given the path.
#
# + path - String value of file path.
# + return - True if path is absolute, else false
public function exists(@untainted string path) returns boolean {
    return externExists(java:fromString(path));
}

function externExists(handle path) returns boolean = @java:Method {
    class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "exists"
} external;

# Creates a new directory with the specified file name.
# If parentDirs flag is true, Creates a directory in specified path with any necessary parents.
#
# + dir - directory name.
# + parentDirs - Indicates whether the `createDir` should create non-existing parent directories.
# + return - Returns absolute path value of the created directory or an `Error` if failed
public function createDir(@untainted string dir, boolean parentDirs = false) returns string|Error {
    var returnVal = externCreateDir(java:fromString(dir), parentDirs);
    if (returnVal is Error) {
        return returnVal;
    } else {
        return <string>java:toString(returnVal);
    }
}

function externCreateDir(handle dir, boolean parentDirs) returns handle|Error = @java:Method {
    class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "createDir"
} external;

# Removes the specified file or directory.
# If recursive flag is true, Removes the path and any children it contains.
#
# + path - String value of file/directory path.
# + recursive - Indicates whether the `remove` should recursively remove all the file inside the given directory.
# + return - Returns an `Error` if failed to remove.
public function remove(@untainted string path, boolean recursive = false) returns Error? {
    return externRemove(java:fromString(path), recursive);
}

function externRemove(handle path, boolean recursive) returns Error? = @java:Method {
    class: "org.ballerinalang.stdlib.file.nativeimpl.Remove",
    name: "remove"
} external;

# Renames(Moves) old path to new path.
# If new path already exists and it is not a directory, this replaces the file.
#
# + oldPath - String value of old file path.
# + newPath - String value of new file path.
# + return - Returns an `Error` if failed to rename.
public function rename(@untainted string oldPath, @untainted string newPath) returns Error? {
    return externRename(java:fromString(oldPath), java:fromString(newPath));
}

function externRename(handle oldPath, handle newPath) returns Error? = @java:Method {
    class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "rename"
} external;

# Returns the default directory to use for temporary files.
#
# + return - Temporary directory location.
public function tempDir() returns string {
    return <string>java:toString(externTempDir());
}

function externTempDir() returns handle = @java:Method {
    class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "tempDir"
} external;

# Creates a file in specified file path.
# Truncates if file already exists in the given path.
#
# + path - String value of file path.
# + return - Returns absolute path value of the created file or an `Error` if failed
public function createFile(@untainted string path) returns string|Error {
    var returnVal = externCreateFile(java:fromString(path));
    if (returnVal is Error) {
        return returnVal;
    } else {
        return <string>java:toString(returnVal);
    }
}

function externCreateFile(handle path) returns handle|Error = @java:Method {
    class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "createFile"
} external;

# Returns metadata information of the file specified in file path.
#
# + path - String value of the file path.
# + return - Returns FileInfo instance with file metadata or an `Error`
public function getFileInfo(@untainted string path) returns FileInfo|Error {
    return externGetFileInfo(java:fromString(path));
}

function externGetFileInfo(handle path) returns FileInfo|Error = @java:Method {
    class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "getFileInfo"
} external;

# Reads the directory and returns a list of files and directories # inside the specified directory
#
# + path - String value of directory path.
# + maxDepth - The maximum number of directory levels to visit. -1 to indicate that all levels should be visited.
# + return - Returns FileInfo array or an `Error` if there is an error while changing the mode.
public function readDir(@untainted string path, int maxDepth = -1) returns FileInfo[]|Error {
    return externReadDir(java:fromString(path), maxDepth);
}

function externReadDir(handle path, int maxDepth) returns FileInfo[]|Error = @java:Method {
    class: "org.ballerinalang.stdlib.file.nativeimpl.ReadDir",
    name: "readDir"
} external;

# Copy file/directory in old path to new path.
# If new path already exists, this replaces the file.
#
# + sourcePath - String value of old file path.
# + destinationPath - String value of new file path.
# + replaceExisting - Flag to allow replace if file already exists in destination path.
# + return - Returns an `Error` if failed to rename.
public function copy(@untainted string sourcePath, @untainted string destinationPath,
            boolean replaceExisting = false) returns Error? {
    return externCopy(java:fromString(sourcePath), java:fromString(destinationPath), replaceExisting);
}

function externCopy(handle path, handle destinationPath, boolean replaceExisting) returns Error? =
@java:Method {
    class: "org.ballerinalang.stdlib.file.nativeimpl.Copy",
    name: "copy"
} external;
