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

documentation {
    Reference to the file location.
}
public type Path object {
    private string root;

    public new (root){
        init(root);
    }

    documentation {
        Constructs the path.
    }
    extern function init(string r);

    documentation {
        Retrieves the absolute path from the provided location.

        R{{}} Returns the absolute path reference or an error if the path cannot be derived
    }
    extern function toAbsolutePath() returns Path;

    documentation {
        Retreives the absolute path from the provided location.

        R{{}} Returns the absolute path as a string
    }
    public extern function getPathValue() returns string;

    documentation {
        Retreives the name of the file from the provided location.

        R{{}} Returns the name of the file
    }
    public extern function getName() returns string;

    documentation {
        Retreives the extension of the file from the provided location.

        R{{}} Returns the extension of the file. Empty string if no extension.
    }
    public extern function getExtension() returns string;

    documentation {
        Check for existance of the file.

        R{{}} True if the file exists, else false
    }
    public extern function exists() returns boolean;

    documentation {
        Returns the files of folders in the directory.

        R{{}} True if the given file path is a directory. It is false otherwise
    }
    public extern function list() returns Path[]|error;

    documentation {
        Check if given file is a directory

        R{{}} True if directory, else false
    }
    public extern function isDirectory() returns boolean;

    documentation {
        Deletes a file/directory.

        R{{}} Error if the directory/file could not be deleted
    }
    public extern function delete() returns error?;

    documentation {
        Creates a directory.

        R{{}} Error if the directory could not be created
    }
    public extern function createDirectory() returns error?;

    documentation {
        Creates a file.

        R{{}} Error if the file could not be created
    }
    public extern function createFile() returns error?;

    documentation {
        Retrieves the last modified time of the file of directory.

        R{{}} Last modified time or an error if the path cannot be resolved
    }
    public extern function getModifiedTime() returns time:Time|error;

    documentation {
        Copy file or directory to another path.

        P{{target}} The location to copy file or directory
        R{{}} Error if the file could not be copied
    }
    public extern function copyTo(@sensitive Path target) returns error?;

    documentation {
        Move file or directory to another path.

        P{{target}} The location to move file or directory
        R{{}} Error if the file could not be moved
    }
    public extern function moveTo(@sensitive Path target) returns error?;

    documentation {
        Get the enclosing parent directory.

        R{{}} Path of parent folder or error occurred while getting parent directory
    }
    public extern function getParentDirectory() returns Path|error;

    documentation {
        Resolve given path.

        R{{}} Resolved path
    }
    public extern function resolve(string... paths) returns Path;
};
