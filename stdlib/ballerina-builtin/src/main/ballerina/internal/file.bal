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

package ballerina.internal;

import ballerina/time;

@Description { value: "Represents an I/O error that could occur when processing a file"}
public type IOError {
    string message;
    error? cause;
};

@Description {value: "Reference to the file location" }
public type Path object{
    private {
      string root;
    }

    new (root){
        init(root);
    }

    @Description { value: "Constructs the path"}
    native function init(string root);

    @Description { value: "Retrieves the absolute path from the provided location"}
    @Return {value:"Returns the absolute path reference or an error if the path cannot be derived"}
    native function toAbsolutePath() returns Path;

    @Description { value: "Retreives the absolute path from the provided location"}
    @Return {value:"Returns the absolute path string value"}
    public native function getPathValue() returns string;

    @Description {value: "Retreives the name of the file from the provided location"}
    @Return {value:"Returns the name of the file"}
    native function getName() returns string;
};

@Description { value: "Check for existance of the file"}
@Param {value: "path: Refernce to the file location"}
@Return {value: "true if the file exists"}
public native function exists(@sensitive Path path) returns boolean;

@Description { value: "Returns the list of paths in the directory"}
@Param {value: "path: Reference to the file path location"}
@Return {value: "List of file paths in the directory or an I/O error"}
native function list(@sensitive Path path) returns Path [] | IOError;

@Description { value: "Returns if the provided path is a directory"}
@Param {value: "path: Reference to the file path location"}
@Return {value: "true if the given file path is a directory. It is false otherwise"}
native function isDirectory(@sensitive Path path) returns boolean;

@Description {value: "Deletes a file/directory from the specified path"}
@Param {value: "path: Reference to the file path location"}
@Return {value:"error if the directory/file could not be deleted"}
native function delete(@sensitive Path path) returns boolean | IOError;

@Description {value: "Creates a directory in the specified location"}
@Param {value: "path: Reference to the file path location"}
@Return {value : "error if the directory could not be created"}
public native function createDirectory(@sensitive Path path) returns boolean | IOError;

@Description {value: "Creates a file in the specified location"}
@Param {value: "path: Reference to the file path location"}
@Return {value : "error if the file could not be created"}
public native function createFile(@sensitive Path path) returns (boolean | IOError);

@Description {value: "Retrieves the last modified time of the path"}
@Return {value : "Last modified time or an error if the path cannot be resolved"}
public native function getModifiedTime(@sensitive Path path) returns (time:Time | IOError);
