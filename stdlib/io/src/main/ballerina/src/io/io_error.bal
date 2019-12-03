// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# This will be used to construct a ConnectionTimedOutError.
public const CONNECTION_TIMED_OUT = "{ballerina/io}ConnectionTimedOut";
# This will return when connection timed out happen when try to connect to a remote host.
public type ConnectionTimedOutError error<CONNECTION_TIMED_OUT, Detail>;

# This will be used to construct an IO GenericError.
public const GENERIC_ERROR = "{ballerina/io}GenericError";
# Represents generic IO error. The detail record contains the information related to the error.
public type GenericError error<GENERIC_ERROR, Detail>;

# This will be used to construct a AccessDeniedError.
public const ACCESS_DENIED_ERROR = "{ballerina/io}AccessDeniedError";
# This will get returned due to file permission issues.
public type AccessDeniedError error<ACCESS_DENIED_ERROR, Detail>;

# This will be used to construct a FileNotFoundError.
public const FILE_NOT_FOUND_ERROR = "{ballerina/io}FileNotFoundError";
# This will get returned if the file is not available in the given file path.
public type FileNotFoundError error<FILE_NOT_FOUND_ERROR, Detail>;

# This will be used to construct an EofError.
public const END_OF_FILE_ERROR = "{ballerina/io}EoF";
# This will get returned if read operations are performed on a channel after it closed.
public type EofError error<END_OF_FILE_ERROR, Detail>;

# Represents IO module related errors.
public type Error GenericError|ConnectionTimedOutError|AccessDeniedError|FileNotFoundError|EofError;
