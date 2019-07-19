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


# Returns the environment variable value associated with the provided name.
#
# + name - Name of the environment variable
# + return - Environment variable value if it exists, otherwise an empty string
public function getEnv(@untainted string name) returns string = external;

# Returns the current working directory.
#
# + return - Current working directory or an empty string if the current working directory cannot be determined
public function getCurrentDirectory() returns string = external;

# Returns the current user's name.
#
# + return - Current user's name if it can be determined, an empty string otherwise
public function getUsername() returns string = external;

# Returns the current user's home directory path.
#
# + return - Current user's home directory if it can be determined, an empty string otherwise
public function getUserHome() returns string = external;

# Returns a random UUID string.
#
# + return - The random string
public function uuid() returns string = external;

# Reports whether file or directory exists for the given the path.
#
# + path - String value of file path.
# + return - True if path is absolute, else false
public function exists(string path) returns boolean = external;

# Creates a new directory with the specified file name.
# If parentDirs flag is true, Creates a directory in specified path with any necessary parents.
#
# + dir - directory name.
# + parentDirs - Indicates whether the `createDir` should create non-existing parent directories.
# + return - Returns absolute path value of the created directory or an `Error` if failed
public function createDir(string dir, boolean parentDirs = false) returns string|Error = external;

# Removes the specified file or directory.
# If recursive flag is true, Removes the path and any children it contains.
#
# + path - String value of file/directory path.
# + recursive - Indicates whether the `remove` should recursively remove all the file inside the given directory.
# + return - Returns an `Error` if failed to remove.
public function remove(string path, boolean recursive = false) returns Error? = external;

# Renames(Moves) old path to new path.
# If new path already exists and it is not a directory, this replaces the file.
#
# + oldPath - String value of old file path.
# + newPath - String value of new file path.
# + return - Returns an `Error` if failed to rename.
public function rename(string oldPath, string newPath) returns Error? = external;

# Returns the default directory to use for temporary files.
#
# + return - Temporary directory location.
public function tempDir() returns string = external;

# Creates a file in specified file path.
# Truncates if file already exists in the given path.
#
# + path - String value of file path.
# + return - Returns absolute path value of the created file or an `Error` if failed
public function createFile(string path) returns string|Error = external;

# Returns metadata information of the file specified in file path.
#
# + path - String value of the file path.
# + return - Returns FileInfo instance with file metadata or an `Error`
public function getFileInfo(string path) returns FileInfo|Error = external;

# Reads the directory and returns a list of files and directories # inside the specified directory
#
# + path - String value of directory path.
# + return - Returns FileInfo array or an `Error` if there is an error while changing the mode.
public function readDir(@untainted string path) returns FileInfo[]|Error = external;

# Copy file/directory in old path to new path.
# If new path already exists, this replaces the file.
#
# + sourcePath - String value of old file path.
# + destinationPath - String value of new file path.
# + replaceExisting - Flag to allow replace if file already exists in destination path.
# + return - Returns an `Error` if failed to rename.
public function copy(string sourcePath, string destinationPath, boolean replaceExisting = false) returns Error? =
external;
