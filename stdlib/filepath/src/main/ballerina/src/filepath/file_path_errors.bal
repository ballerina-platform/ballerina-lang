// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Represents the details of an error.
#
# + message - The error message.
# + cause - Cause of the error.
type Detail record {
    string message;
    error cause?;
};


# Indicates that a file/directory does not exist at the given filepath
public const FILE_NOT_FOUND_ERROR = "{ballerina/filepath}FileNotFoundError";
public type FileNotFoundError error<FILE_NOT_FOUND_ERROR, Detail>;

# Indicates that the file at the given filepath is not a symbolic link
public const NOT_LINK_ERROR = "{ballerina/filepath}NotLinkError";
public type NotLinkError error<NOT_LINK_ERROR, Detail>;

# Indicates that an IO error occurred when trying to access the file at the given filepath
public const IO_ERROR = "{ballerina/filepath}IOError";
public type IOError error<IO_ERROR, Detail>;

# Indicates that a security error occurred when trying to access the file at the given filepath
public const SECURITY_ERROR = "{ballerina/filepath}SecurityError";
public type SecurityError error<SECURITY_ERROR, Detail>;

# Indicates that the given file path is invalid
public const INVALID_PATH_ERROR = "{ballerina/filepath}InvalidPathError";
public type InvalidPathError error<INVALID_PATH_ERROR, Detail>;

# Indicates that the given pattern does not confirm to the java FileSystem PathMatcher
# references: https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)
public const INVALID_PATTERN_ERROR = "{ballerina/filepath}InvalidPatternError";
public type InvalidPatternError error<INVALID_PATTERN_ERROR, Detail>;

# Indicates that the given target filepath cannot be made relative to the base filepath
public const RELATIVE_PATH_ERROR = "{ballerina/filepath}RelativePathError";
public type RelativePathError error<RELATIVE_PATH_ERROR, Detail>;

# Indicates that an error has occured with the UNC path
public const UNC_PATH_ERROR = "{ballerina/filepath}UNCPathError";
public type UNCPathError error<UNC_PATH_ERROR, Detail>;

# Generic error for filepath
public const GENERIC_ERROR = "{ballerina/filepath}GenericError";
public type GenericError error<GENERIC_ERROR, Detail>;

public type ErrorType FILE_NOT_FOUND_ERROR | NOT_LINK_ERROR | IO_ERROR | SECURITY_ERROR | INVALID_PATH_ERROR |
INVALID_PATTERN_ERROR | RELATIVE_PATH_ERROR | UNC_PATH_ERROR | GENERIC_ERROR;

public type Error FileNotFoundError | NotLinkError | IOError | SecurityError | InvalidPathError |
InvalidPatternError | RelativePathError | UNCPathError | GenericError;

# Prepare the `error` as a `Error`.
# + errorType - The type of error
# + message - The error message.
# + err - The `error` instance.
# + return - Prepared `Error` instance.
public function prepareError(ErrorType errorType, string message, error? err = ()) returns Error {
    error filePathError;
    if (err is error) {
	    filePathError = error(errorType, message = message, cause = err);
    } else {
        filePathError = error(errorType, message = message);
    }
    return <Error> filePathError;
}
