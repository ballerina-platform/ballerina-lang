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

@Description { value: "Represents an I/O error which could occur when processing a file."}
public type IOError {
    string message;
    error? cause;
};

@Description {value: "Reference to the file location" }
public type Path object{
    private {
      string link;
    }

    new (link){
        init(link);
    }

    @Description { value: "Constructs the path"}
    native function init(string link);

    @Description { value: "Retreives the absolut path from the provided location"}
    @Return {value:"Returns the absolute path reference or an error if the path cannot be derived."}
    public native function toAbsolutePath() returns (Path);

    @Description { value: "Retreives the absolut path from the provided location"}
    @Return {value:"Returns the absolute path string value"}
    public native function getPathValue() returns (string);
};

@Description { value: "Check for existance of the file"}
@Param {value: "path: Refernce to the file location"}
@Return {value: "true if the file exists"}
public native function exists(Path path) returns (boolean);

@Description { value: "Returns the list of paths in directory"}
@Param {value: "path: Refernce to the file path location"}
@Return {value: "List of file paths in the directory or an io error"}
public native function list(Path path) returns (Path [] | IOError);

@Description { value: "Returns if the provided path is a directory"}
@Param {value: "path: Refernce to the file path location"}
@Return {value: "true if the given file path is a directory, false otherwise"}
public native function isDirectory(Path path) returns (boolean);

@Description {value: "Deletes a file/directory from the specified path"}
@Param {value: "path: Refernce to the file path location"}
@Return {value:"error if the directory/file could not be deleted"}
public native function delete(Path path) returns (boolean | IOError);

@Description {value: "Creates a directory in the specified location"}
@Param {value: "path: Refernce to the file path location"}
@Return {value : "error if the directory could not be created"}
public native function createDirectory(Path path) returns (boolean | IOError);

@Description {value: "Creates a file in the specified location"}
@Param {value: "path: Refernce to the file path location"}
@Return {value : "error if the file could not be created"}
public native function createFile(Path path) returns (boolean | IOError);
