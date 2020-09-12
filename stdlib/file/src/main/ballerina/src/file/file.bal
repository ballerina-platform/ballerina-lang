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

import ballerina/java;

# Returns the current working directory.
# ```ballerina
# string dirPath = file:getCurrentDirectory();
# ```
# 
# + return - Current working directory or else an empty string if the current working directory cannot be determined
public function getCurrentDirectory() returns string = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "getCurrentDirectory"
} external;

# Reports whether the file or directory exists in the given the path.
# ```ballerina
# boolean result = file:exists("foo/bar.txt");
# ```
#
# + path - String value of the file path
# + return - True if the path is absolute or else false
public function exists(@untainted string path) returns boolean = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "exists"
} external;

# Creates a new directory with the specified file name.
# If the `parentDirs` flag is true, it creates a directory in the specified path with any necessary parents.
# ```ballerina
# string | error results = file:createDir("foo/bar");
# ```
#
# + dir - Directory name
# + parentDirs - Indicates whether the `createDir` should create non-existing parent directories
# + return - Absolute path value of the created directory or else an `file:Error` if failed
public function createDir(@untainted string dir, boolean parentDirs = false) returns string|Error = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "createDir"
} external;

# Removes the specified file or directory.
# If the recursive flag is true, it removes the path and any children it contains.
# ```ballerina
# file:Error? results = file:remove("foo/bar.txt");
# ```
#
# + path - String value of the file/directory path
# + recursive - Indicates whether the `remove` should recursively remove all the files inside the given directory
# + return - An `file:Error` if failed to remove
public function remove(@untainted string path, boolean recursive = false) returns Error? = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "remove"
} external;

# Renames(Moves) the old path with the new path.
# If the new path already exists and it is not a directory, this replaces the file.
# ```ballerina
# file:error? results = file:rename("/A/B/C", "/A/B/D");
# ```
#
# + oldPath - String value of the old file path
# + newPath - String value of the new file path
# + return - An `file:Error` if failed to rename
public function rename(@untainted string oldPath, @untainted string newPath) returns Error? = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "rename"
} external;

# Returns the default directory to use for temporary files.
# ```ballerina
# string results = file:tempDir();
# ```
#
# + return - Temporary directory location
public function tempDir() returns string = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "tempDir"
} external;

# Creates a file in the specified file path.
# Truncates if the file already exists in the given path.
# ```ballerina
# string | error results = file:createFile("bar.txt");
# ```
#
# + path - String value of the file path
# + return - Absolute path value of the created file or else an `file:Error` if failed
public function createFile(@untainted string path) returns string|Error = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "createFile"
} external;

# Returns the metadata information of the file specified in the file path.
# ```ballerina
# file:FileInfo | error result = file:getFileInfo("foo/bar.txt");
# ```
#
# + path - String value of the file path.
# + return - The `FileInfo` instance with the file metadata or else an `file:Error`
public function getFileInfo(@untainted string path) returns FileInfo|Error = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "getFileInfo"
} external;

# Reads the directory and returns a list of files and directories 
# inside the specified directory.
# ```ballerina
# file:FileInfo[] | error results = file:readDir("foo/bar");
# ```
#
# + path - String value of the directory path.
# + maxDepth - The maximum number of directory levels to visit. -1 to indicate that all levels should be visited
# + return - The `FileInfo` array or else an `file:Error` if there is an error while changing the mode.
public function readDir(@untainted string path, int maxDepth = -1) returns FileInfo[]|Error = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "readDir"
} external;

# Copy the file/directory in the old path to the new path.
# If a file already exists in the new path, this replaces that file.
# ```ballerina
# file:Error? results = file:copy("/A/B/C", "/A/B/D", true);
# ```
#
# + sourcePath - String value of the old file path
# + destinationPath - String value of the new file path
# + replaceExisting - Flag to replace if the file already exists in the destination path
# + return - An `file:Error` if failed to rename
public function copy(@untainted string sourcePath, @untainted string destinationPath,
                     boolean replaceExisting = false) returns Error? = @java:Method {
    'class: "org.ballerinalang.stdlib.file.nativeimpl.Utils",
    name: "copy"
} external;
