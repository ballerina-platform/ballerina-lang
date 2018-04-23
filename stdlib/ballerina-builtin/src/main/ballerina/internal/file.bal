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
    Represents an I/O error that could occur when processing a file.
}
public type IOError {
    string message;
    error? cause;
};

documentation {
    Reference to the file location.
}
public type Path object {
    private {
        string root;
    }

    new (root) {
        init(root);
    }

    documentation {
        Constructs the path.
    }
    native function init(string root);

    documentation {
        Retrieves the absolute path from the provided location.

        R{{}} Returns the absolute path reference or an error if the path cannot be derived
    }
    native function toAbsolutePath() returns Path;

    documentation {
            Retreives the absolute path from the provided location.

            R{{}} Returns the absolute path string value
        }
    public native function getPathValue() returns string;

    documentation {
            Retreives the name of the file from the provided location.

            R{{}} Returns the name of the file
        }
    native function getName() returns string;
};

documentation {
    Check for existance of the file.

    P{{path}} Refernce to the file location
    R{{}} true if the file exists
}
public native function pathExists(@sensitive Path path) returns boolean;

documentation {
    Returns the list of paths in the directory.

    P{{path}} Reference to the file path location
    R{{}} List of file paths in the directory or an I/O error
}
native function list(@sensitive Path path) returns Path[]|IOError;

documentation {
    Returns if the provided path is a directory.

    P{{path}} Reference to the file path location
    R{{}} true if the given file path is a directory. It is false otherwise
}
native function isDirectory(@sensitive Path path) returns boolean;

documentation {
    Deletes a file/directory from the specified path.

    P{{path}} Reference to the file path location
    R{{}} error if the directory/file could not be deleted
}
native function delete(@sensitive Path path) returns boolean|IOError;

documentation {
    Creates a directory in the specified location.

    P{{path}} Reference to the file path location
    R{{}} error if the directory could not be created
}
public native function createDirectory(@sensitive Path path) returns boolean|IOError;

documentation {
    Creates a file in the specified location.

    P{{path}} Reference to the file path location
    R{{}} error if the file could not be created
}
public native function createFile(@sensitive Path path) returns (boolean|IOError);

documentation {
    Retrieves the last modified time of the path.

    R{{}} Last modified time or an error if the path cannot be resolved
}
public native function getModifiedTime(@sensitive Path path) returns (time:Time|IOError);
