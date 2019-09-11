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

const INVALID_OPERATION_ERROR = "{ballerina/file}InvalidOperationError";

# Represents error occur when a file system operation is denied, due to invaild operation.
public type InvalidOperationError error<INVALID_OPERATION_ERROR, Detail>;

const PERMISSION_ERROR = "{ballerina/file}PermissionError";
# Represents error occur when a file system operation is denied, due to file permission.
public type PermissionError error<PERMISSION_ERROR, Detail>;

const FILE_SYSTEM_ERROR = "{ballerina/file}FileSystemError";
# Represents error occur when file system operation is failed.
public type FileSystemError error<FILE_SYSTEM_ERROR, Detail>;

const FILE_NOT_FOUND_ERROR = "{ballerina/file}FileNotFoundError";
# Represents error occur when the file/directory does not exist at the given filepath.
public type FileNotFoundError error<FILE_NOT_FOUND_ERROR, Detail>;

# Represents file system related errors.
public type Error InvalidOperationError|PermissionError|FileSystemError|FileNotFoundError;
