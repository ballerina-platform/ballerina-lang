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

# Represents an error that occurs when a file system operation is denied due to invalidity.
public type InvalidOperationError distinct error;

# Represents an error that occurs when a file system operation is denied, due to the absence of file permission.
public type PermissionError distinct error;

# Represents an error that occurs when a file system operation fails.
public type FileSystemError distinct error;

# Represents an error that occurs when the file/directory does not exist at the given filepath.
public type FileNotFoundError distinct error;

# Represents file system related errors.
public type Error InvalidOperationError|PermissionError|FileSystemError|FileNotFoundError;
