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

# Record type to hold the details of an error.
#
# + message - Specific error message of the error.
# + cause - Any other error, which causes this error.
public type Detail record {
    string message;
    error cause?;
};

# Identifies file not found error.
public const FILE_NOT_FOUND_ERROR = "{ballerina/filepath}FileNotFoundError";
# Represents error occur when the file/directory does not exist at the given filepath.
public type FileNotFoundError error<FILE_NOT_FOUND_ERROR, Detail>;

# Identifies no link error.
public const NOT_LINK_ERROR = "{ballerina/filepath}NotLinkError";
# Represents error occur when the file at the given filepath is not a symbolic link.
public type NotLinkError error<NOT_LINK_ERROR, Detail>;

# Identifies io error.
public const IO_ERROR = "{ballerina/filepath}IOError";
# Represents IO error occur when trying to access the file at the given filepath.
public type IOError error<IO_ERROR, Detail>;

# Identifies security error.
public const SECURITY_ERROR = "{ballerina/filepath}SecurityError";
# Represents security error occur when trying to access the file at the given filepath.
public type SecurityError error<SECURITY_ERROR, Detail>;

# Identifies invalid path error.
public const INVALID_PATH_ERROR = "{ballerina/filepath}InvalidPathError";
# Represents error occur when the given file path is invalid.
public type InvalidPathError error<INVALID_PATH_ERROR, Detail>;

# Identifies invalid pattern error.
public const INVALID_PATTERN_ERROR = "{ballerina/filepath}InvalidPatternError";
# Represent error occur when the given pattern is not a valid filepath pattern.
public type InvalidPatternError error<INVALID_PATTERN_ERROR, Detail>;

# Identifies relative path error.
public const RELATIVE_PATH_ERROR = "{ballerina/filepath}RelativePathError";
# Represents an error that occurs when the given target filepath cannot be derived relative to the base filepath.
public type RelativePathError error<RELATIVE_PATH_ERROR, Detail>;

# Identifies unc path error.
public const UNC_PATH_ERROR = "{ballerina/filepath}UNCPathError";
# Represents error occur in the UNC path.
public type UNCPathError error<UNC_PATH_ERROR, Detail>;

# Identifies generic error.
public const GENERIC_ERROR = "{ballerina/filepath}GenericError";
# Represents generic error for filepath
public type GenericError error<GENERIC_ERROR, Detail>;

# Represents filepath related error types.
type ErrorType FILE_NOT_FOUND_ERROR | NOT_LINK_ERROR | IO_ERROR | SECURITY_ERROR | INVALID_PATH_ERROR |
INVALID_PATTERN_ERROR | RELATIVE_PATH_ERROR | UNC_PATH_ERROR | GENERIC_ERROR;

# Represents filepath related errors.
public type Error FileNotFoundError | NotLinkError | IOError | SecurityError | InvalidPathError |
InvalidPatternError | RelativePathError | UNCPathError | GenericError;

# Prepare the `error` as a `Error`.
# + errorType - The type of error.
# + message - The error message.
# + err - The `error` instance.
# + return - Prepared `Error` instance.
function prepareError(ErrorType errorType, string message, error? err = ()) returns Error {
    error filePathError;
    if (err is error) {
	    filePathError = error(errorType, message = message, cause = err);
    } else {
        filePathError = error(errorType, message = message);
    }
    return <Error> filePathError;
}
