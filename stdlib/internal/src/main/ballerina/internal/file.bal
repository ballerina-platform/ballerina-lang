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

import ballerina/time;

# Reference to the file location.
public type Path object {
    private string root;

    public new (root){
        init(root);
    }

    # Constructs the path.
    native function init(string r);

    # Retrieves the absolute path from the provided location.
    #
    # + return - Returns the absolute path reference or an error if the path cannot be derived
    native function toAbsolutePath() returns Path;

    # Retreives the absolute path from the provided location.
    #
    # + return - Returns the absolute path as a string
    public native function getPathValue() returns string;

    # Retreives the name of the file from the provided location.
    #
    # + return - Returns the name of the file
    public native function getName() returns string;

    # Retreives the extension of the file from the provided location.
    #
    # + return - Returns the extension of the file. Empty string if no extension.
    public native function getExtension() returns string;

    # Check for existance of the file.
    #
    # + return - True if the file exists, else false
    public native function exists() returns boolean;

    # Returns the files of folders in the directory.
    #
    # + return - True if the given file path is a directory. It is false otherwise
    public native function list() returns Path[]|error;

    # Check if given file is a directory
    #
    # + return - True if directory, else false
    public native function isDirectory() returns boolean;

    # Deletes a file/directory.
    #
    # + return - Error if the directory/file could not be deleted
    public native function delete() returns error?;

    # Creates a directory.
    #
    # + return - Error if the directory could not be created
    public native function createDirectory() returns error?;

    # Creates a file.
    #
    # + return - Error if the file could not be created
    public native function createFile() returns error?;

    # Retrieves the last modified time of the file of directory.
    #
    # + return - Last modified time or an error if the path cannot be resolved
    public native function getModifiedTime() returns time:Time|error;

    # Copy file or directory to another path.
    #
    # + target - The location to copy file or directory
    # + return - Error if the file could not be copied
    public native function copyTo(@sensitive Path target) returns error?;

    # Move file or directory to another path.
    #
    # + target - The location to move file or directory
    # + return - Error if the file could not be moved
    public native function moveTo(@sensitive Path target) returns error?;

    # Get the enclosing parent directory.
    #
    # + return - Path of parent folder or error occurred while getting parent directory
    public native function getParentDirectory() returns Path|error;

    # Resolve given path.
    #
    # + return - Resolved path
    public native function resolve(string... paths) returns Path;
};
